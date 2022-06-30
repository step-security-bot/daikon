# Daikon Spring License
#### (for on-prem / hybrid apps)

This library contains code that manages user sessions for on-prem apps. It ensures
that given user has only one concurrent session (so it prevents 2 people from using apps
with the same user account, thus circumventing license user limitations). This code 
is taken from `oidc-client`.

It should be used with on-prem apps (like TDP, TDS, MDM). Cloud apps don't need it.
But it _can_ be used with cloud apps, it just will not have any effect (this is done
to support hybrid apps).

The way it works, in short, is by adding a filter to Zuul in App Gateway, which 
intercepts all requests from the User to the App. Then the filter checks with OIDC
whether there are any other sessions for this user in parallel. If such sessions
exist, the filter throws `BadCredentialsException` to signal that user session should
be terminated. This results in the next response from the filter:
```
{
    "timestamp":1496186099916,
    "status":401,
    "error":"Unauthorized",
    "exception":"com.netflix.zuul.exception.ZuulException",
    "message":"pre:KeepAliveLicenseFilter"
}
```
Otherwise, if only one session for given user exists, the filter lets the request 
be processed as usual (i.e. forward it to the backend service).

### Details

In OIDC there's a custom endpoint `/licenses`, which has two API calls:

`/keepAlive` - checks if there is a concurrent session for given user. If so, it returns
http code `400`. This signals to Zuul filter to terminate user session.

`/config` - returns the interval (in seconds) that the filter should use 
to call `keepAlive` endpoint. This is done to prevent putting too much load on OIDC.
By default, this value is set to 30, which means the Zuul filter should check
user session every 30 seconds.

Internally, OIDC maintains a separate table in its DB which it uses to document all
sessions for users, including renewals. This is done to ignore stale sessions, which
happens when user closes browser without explicitly logging out. Effectively, only
the most recent session is kept, all others would return `400` if `/keepAlive`
endpoint is called on them. OIDC has a cleaner job that removes stale sessions
periodically.

The Zuul filter works like this:
1. When it handles the first request, it calls `/config` endpoint to find out the interval
2. On each request, it checks if a `keepAlive` request was made within the interval
3. If yes, it does nothing (lets the request be processed as usual)
4. If no, it makes a request to `/keepAlive` endpoint
5. If this request returns http error code `400`, the filter returns http `401` to user
6. Otherwise, it remembers the request timestamp and lets the request be processed

This library consists of two major components: License client and Zuul filter.

### License Client

Package: `org.talend.daikon.spring.license.client`

This package contains client used to call `/license` endpoints in OIDC, and auto-config
for it. It can be referenced by the App Gateway as a bean of type `LicenseClient`
(although, it's not usually necessary, as it will be used primarily by gateway filter).

The client provides two methods, namely `getConfig` and `keepAlive` to make requests
to `/license` endpoint in OIDC. It's a standard REST client implementation using
spring `RestTemplate` class. It requires one configuration parameter, license service
base url, which is described below.

### Zuul Gateway Filter

Package: `org.talend.daikon.spring.license.zuul`

This package contains Zuul filter and configuration for it, which uses License Client
bean to make calls to license service and check user sessions. If user has concurrent
sessions, all older sessions will be intercepted by the filter, and http code `401`
will be returned.

### Custom Filter

Since Spring Boot 2.x has dropped support for Zuul (it still kinda works, but not 
officially supported by Spring), it might be necessary to create a custom filter
for the same functionality (if an app decides to migrate to a new gateway impl).

This requires to next steps to be done:
1. Chose the replacement gateway implementation (for example, Spring Cloud Gateway)
2. Find out how filter/interceptor system works for the new implementation
3. The goal is to be able to intercept and terminate any request with authentication
4. Use `KeepAliveLicenseFilter` as an example to write a filter for new gateway

## Usage

This library provides spring autoconfiguration factories, that use configuration
parameters listed below.

For `LicenseClient` bean to be created, `iam.license.url` property has to be defined.

For Zuul Gateway Filter to be created, the next conditions have to be met:
1. `LicenseClient` bean should be provided (see above).
2. `spring-cloud-starter-netflix-zuul` has to be in dependencies.
3. `spring-security-oauth2` has to be in dependencies.

When all conditions are met, the `LicenseClient` bean will be created and also the filter,
which will be handling the requests. No further actions are needed.

### Configuration

| Property        | Description                                  | Example                        |
|-----------------|----------------------------------------------|--------------------------------|
| iam.license.url | Base URL that LicenseClient uses to call IAM | http://localhost:9080/oidc/api |
