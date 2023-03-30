package org.talend.daikon.spring.audit.logs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.servlet.http.HttpServletRequest;

public class AuditLogUrlExtractorImplTest {

    private AuditLogUrlExtractor auditLogUrlExtractor = new AuditLogUrlExtractorImpl();

    @Test
    public void testXFHShouldSucceedMultipleHosts() {
        // Given
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        when(requestMock.getHeader(eq("x-forwarded-host"))).thenReturn("host1,host2");
        when(requestMock.getHeader(eq("x-forwarded-proto"))).thenReturn("https");

        // When
        String uri = auditLogUrlExtractor.extract(requestMock);

        assertEquals("https://host1", uri);
        verify(requestMock, times(1)).getHeader(eq("x-forwarded-host"));
    }

    @Test
    public void testXFHShouldSucceedSingleHost() {
        // Given
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        when(requestMock.getHeader(eq("x-forwarded-host"))).thenReturn("host1");
        when(requestMock.getHeader(eq("x-forwarded-proto"))).thenReturn("https");

        // When
        String uri = auditLogUrlExtractor.extract(requestMock);

        assertEquals("https://host1", uri);
        verify(requestMock, times(1)).getHeader(eq("x-forwarded-host"));
    }

    @Test
    public void testHShouldSucceedMultipleHosts() {
        // Given
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        when(requestMock.getHeader(eq("host"))).thenReturn("host1,host2");
        when(requestMock.getHeader(eq("x-forwarded-proto"))).thenReturn("https");

        // When
        String uri = auditLogUrlExtractor.extract(requestMock);

        assertEquals("https://host1", uri);
        verify(requestMock, times(1)).getHeader(eq("host"));
    }

    @Test
    public void testHShouldSucceedSingleHost() {
        // Given
        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        when(requestMock.getHeader(eq("host"))).thenReturn("host1");
        when(requestMock.getHeader(eq("x-forwarded-proto"))).thenReturn("https");

        // When
        String uri = auditLogUrlExtractor.extract(requestMock);

        assertEquals("https://host1", uri);
        verify(requestMock, times(1)).getHeader(eq("host"));
    }
}
