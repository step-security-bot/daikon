# Functional Context generation with @FunctionalContext

[The functional context](https://github.com/Talend/architecture-work/blob/master/security/api_security/documentation.md#security-context-vs-functional-context)
at Talend in generally extracted from the JWT used for authentication. In some use cases, there will be no functional
context in the security context. For example in case of Client Credential flow.

## What ?

The functional context needs to be generated. A JWT contain all the information about the user, but in case of generated
context, we want more fined grained context for:

* Security reason
* Limiting the performance side effect

## How it works?

### @M2MFunctionalContext Annotation

Use the annotation `@M2MFunctionalContext` on each controller method that needs a context. It will automatically get
the `tenantId` and `userId` from either the path or the request parameter. The tenantId must be in the path.

``` java
@M2MFunctionalContext
public ResponseEntity<List<SharingResponse>> getSharingByEntityTypeAdmin(String entityType,
                                                                         String xClientVersion,
                                                                    Boolean includeMetadata) {
  ...
}
```

#### Configuration

You need to add configuration for your annotation to work.

##### Customisation of the context holder

We have more than one way to hold the Functional Context (like
in [daikon-multitenant](https://github.com/Talend/daikon/blob/master/daikon-multitenant/multitenant-core/src/main/java/org/talend/daikon/multitenant/context/TenancyContextHolder.java)
or in
the [provided implementation](src/main/java/org/talend/daikon/spring/ccf/context/provided/DefaultTenantContextHolder.java)).

The context holder can be customized by implementing
the [ContextManager](src/main/java/org/talend/daikon/spring/ccf/context/M2MContextManager.java) and make it available as
a Spring bean.

```java
public class TenantContextManager implements M2MContextManager {
    @Override
    public void clearContext() {
        DefaultTenantContextHolder.clear();
    }

    @Override
    public void injectContext(String tenantId, String userId, Optional<User> user) {
        // USE THOSE INFORMATION TO POPULATE YOUR OWN CONTEXT HOLDER
        DefaultTenantContextHolder.setContextWithTenantId(tenantId);
        DefaultTenantContextHolder.getContext().setUserId(userId);
        user.ifPresent(DefaultTenantContextHolder.getContext()::setCurrentUser);
    }
}
```

##### How to get the params

In all use cases where we need to generate the functional context, **the tenantId is mandatory**.
Based on
the [latest talend policies on Admin API](https://github.com/Talend/architecture-work/blob/master/security/api_security/documentation.md#admin-api),
the tenantId must be in the path on the request.

**The userId is mandatory in case you need Talend user context.**
It can be either in the path or in a parameters.
The default [implementation](src/main/java/org/talend/daikon/spring/ccf/context/TenantParameterExtractorImpl.java) will
take both from the path.

You can override this behavior by making available a
bean [TenantParameterExtractor](src/main/java/org/talend/daikon/spring/ccf/context/TenantParameterExtractor.java). And
implement `extractAccountId` and `extractUserId`.

#### Possible errors

1. TenantId is not in the path / not a UUID / null will lead to `404`
2. UserId is null/not a UUID and annotation ask for user context will lead warning logs
3. SCIM is unavailable and needed, it will lead to `500`

#### Customisation of the context

In case tenantId and userId are not enough, the context can be customised as following:

User context can be retrieved by adding any of
the [constants](src/main/java/org/talend/daikon/spring/ccf/context/UserContextConstant.java) in `userContext`. The
default is `NONE`.

``` java
@M2MFunctionalContext(userContext = {GROUPS, TIMEZONE})
```

#### Caching

Since SCIM service can be called to populate the context if needed, we made it mandatory to use cache. If you don't have
any cache configured
then [the default cache](src/main/java/org/talend/daikon/spring/ccf/context/configuration/M2MFunctionalContextConfig.java)
will be used.

##### Cache Customization

* To use your own cache, make
  sure [CacheManager](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/CacheManager.html)
  bean is available
* By default, the cache name will be `ccfScimCache`
* Use `spring.ccf.context.cache.name` to customize the name if you want to reuse an existing one.
* The cache is cleared based on `spring.ccf.context.cache.ttl` in days. **Default value is 5**.

# Contact

Reach out to #ask-platform-services in Slack for any help or question.
