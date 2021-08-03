# Daikon spring Reactive Security

This module contains a reactive WebClient which overrides the existing one from `spring-webflux`

## Authenticated Call

Authenticated calls are needed to call API like dataset, sharing, rating, ...
Those API are waiting a JWT token from a header `Authorization`.

In order to use reactive with non-blocking request, we need to pass through each request the token.
Otherwise, we lose the token between calls (example: `flatMap()`)

## Exchange Filter

This filter uses the reactor context to store the token and pass it through the next request using the `flatMap(next:exchange)`.

In order to use it add a `WebClient.Builder` bean like this one on your project:

```java
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient
                .builder()
                .filter(new JwtBearerExchangeFilterFunction());
    }
```

The context with the token need to be subscribed to the request.
So, this is an example to authenticate a call (`SharingWebClient`) :

```java
    public static <T> Mono<T> addSecurityContext(Mono<T> monoResponse) {
        return ReactiveSecurityContextHolder
        .getContext() //
        .map(SecurityContext::getAuthentication) //
        .flatMap(auth -> //
        monoResponse.subscriberContext(ReactiveSecurityContextHolder.withAuthentication(auth)));
        }

    addSecurityContext(sharingClient.getSharings(EntityType.DATASET)
        .flatMap(sharings -> sharingClient.getSharingSetEntity(EntityType.DATASET, sharings.getEntityId())));
```

## ReactiveTenancyContextHolder

If you need to access tenancy context inside a reactive chain of call you need to add the JwtTenancyContextWebFilter on your `SecurityWebFilterChain`

```java

    private static final ServerWebExchangeMatcher AUTH_ALLOW_LIST =
            ServerWebExchangeMatchers.pathMatchers(            
                // -- swagger ui
                    "/swagger-resources/**",
                    "/swagger-ui/**",
                    "/v2/api-docs",
                // -- actuators
                    "/info/**",
                    "/health/**",
                    "/metrics/**"
            );

    @Bean
    public SecurityWebFilterChain publicApiSecurityWebFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(AUTH_ALLOW_LIST)
                .csrf()
                .disable() // we can disable CSRF because this service only use JWT token through Authorization header
                .authorizeExchange()
                .anyExchange()
                .permitAll();
        return http.build();
    }

    @Bean
    public SecurityWebFilterChain privateApiSecurityWebFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(new NegatedServerWebExchangeMatcher(AUTH_ALLOW_LIST))
                // Tenancy Context Filter need to be add after Security Context Server because it will used
                // Authentication to get the tenantId
                .addFilterAfter(new JwtTenancyContextWebFilter(), SecurityWebFiltersOrder.AUTHORIZATION)
                .csrf()
                .disable() // we can disable CSRF because this service only use JWT token through Authorization header
                .authorizeExchange()
                .anyExchange()
                .authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(grantedAuthoritiesExtractor());
        return http.build();
    }
```
You can now access to tenant context inside a reactive chain with:

```java
 return ReactiveTenancyContextHolder
    .getContext() //
    .map(TenancyContext::getTenant) //
    .map(tenant -> // do what you want with tenant data);
```
