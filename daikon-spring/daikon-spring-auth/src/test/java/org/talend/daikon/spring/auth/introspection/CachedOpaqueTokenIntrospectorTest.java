package org.talend.daikon.spring.auth.introspection;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.talend.daikon.spring.auth.interceptor.IpAllowListHeaderInterceptor.X_FORWARDED_FOR;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

@ExtendWith(MockitoExtension.class)
public class CachedOpaqueTokenIntrospectorTest {

    private static final String TOKEN = "token";

    @Mock
    private Cache cache;

    @Mock
    private OpaqueTokenIntrospector delegate;

    @InjectMocks
    private CachedOpaqueTokenIntrospector introspector;

    @BeforeEach
    public void setUp() {
        RequestContextHolder.setRequestAttributes(null);
    }

    @Test
    public void nullParametersNotAcceptedByConstructor() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CachedOpaqueTokenIntrospector(null, cache));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CachedOpaqueTokenIntrospector(delegate, null));
    }

    @Test
    public void concurrentMapCache() {
        // given
        ConcurrentMapCache concurrentMapCache = Mockito.spy(new ConcurrentMapCache("name"));
        introspector = new CachedOpaqueTokenIntrospector(delegate, concurrentMapCache);

        AuthUserDetails expected = new AuthUserDetails("username", "password", singletonList(new SimpleGrantedAuthority("test")));
        expected.setEmail("email@test.com");
        expected.setId("user-id");

        when(delegate.introspect(TOKEN)).thenReturn(expected);

        // when
        AuthUserDetails resultFromDelegate = (AuthUserDetails) introspector.introspect(TOKEN);
        verify(concurrentMapCache, times(1)).get(TOKEN);
        verify(delegate, times(1)).introspect(TOKEN);
        verify(concurrentMapCache, times(1)).put(TOKEN, expected);

        AuthUserDetails resultFromCache = (AuthUserDetails) introspector.introspect(TOKEN);
        verify(concurrentMapCache, times(2)).get(TOKEN); // retrieved from cache
        verify(delegate, times(1)).introspect(TOKEN); // still one
        verify(concurrentMapCache, times(1)).put(TOKEN, expected); // still one

        // then
        Assertions.assertNotNull(resultFromDelegate);
        Assertions.assertNotNull(resultFromCache);

        Assertions.assertEquals("username", resultFromCache.getUsername());
        Assertions.assertEquals("user-id", resultFromCache.getId());
        Assertions.assertEquals("email@test.com", resultFromCache.getEmail());
        Assertions.assertEquals("test", resultFromCache.getAuthorities().iterator().next().getAuthority());

        Assertions.assertEquals(expected, resultFromDelegate);
        Assertions.assertEquals(expected, resultFromCache);
    }

    @Test
    public void retrieveFromCache() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(cache.get(TOKEN)).thenReturn(new SimpleValueWrapper(expected));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).get(TOKEN);
        verify(delegate, times(0)).introspect(TOKEN);

        Assertions.assertNotNull(principal);
        Assertions.assertSame(expected, principal);
    }

    @Test
    public void retrieveCallingDelegate() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(delegate.introspect(TOKEN)).thenReturn(expected);

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).get(TOKEN);
        verify(delegate, times(1)).introspect(TOKEN);

        Assertions.assertNotNull(principal);
        Assertions.assertSame(expected, principal);
    }

    @Test
    public void putIntoCache() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(delegate.introspect(TOKEN)).thenReturn(expected);

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).put(TOKEN, principal);
    }

    @Test
    public void putIntoCacheThrowsException() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(delegate.introspect(TOKEN)).thenReturn(expected);
        doThrow(new RuntimeException("cache put failed")).when(cache).put(any(), any());

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).get(TOKEN);
        verify(delegate, times(1)).introspect(TOKEN);
        verify(cache, times(1)).put(TOKEN, expected);

        Assertions.assertNotNull(principal);
        Assertions.assertSame(expected, principal);
    }

    @Test
    public void retrieveFromCacheThrowsException() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(delegate.introspect(TOKEN)).thenReturn(expected);
        when(cache.get(any())).thenThrow(new RuntimeException("find in cache failed"));

        // when
        OAuth2AuthenticatedPrincipal principal = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).get(TOKEN);
        verify(delegate, times(1)).introspect(TOKEN);

        Assertions.assertNotNull(principal);
        Assertions.assertSame(expected, principal);
    }

    @Test
    public void cacheKeyIsDifferentForDifferentCallerIP() {
        // given
        OAuth2AuthenticatedPrincipal expected = mock(OAuth2AuthenticatedPrincipal.class);
        when(delegate.introspect(TOKEN)).thenReturn(expected);

        // when
        mockXFFHeader("1.1.1.1");
        OAuth2AuthenticatedPrincipal firstResult = introspector.introspect(TOKEN);

        mockXFFHeader("2.2.2.2");
        OAuth2AuthenticatedPrincipal secondResult = introspector.introspect(TOKEN);

        // then
        verify(cache, times(1)).get(TOKEN + "1.1.1.1".hashCode());
        verify(cache, times(1)).get(TOKEN + "2.2.2.2".hashCode());
        verify(delegate, times(2)).introspect(TOKEN);
        verify(cache, times(1)).put(TOKEN + "1.1.1.1".hashCode(), expected);
        verify(cache, times(1)).put(TOKEN + "2.2.2.2".hashCode(), expected);

        Assertions.assertNotNull(firstResult);
        Assertions.assertNotNull(secondResult);
        Assertions.assertSame(expected, firstResult);
        Assertions.assertSame(expected, secondResult);

        // clean up
        RequestContextHolder.setRequestAttributes(null);
    }

    private static void mockXFFHeader(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn(ip);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

}
