package org.talend.daikon.spring.audit.logs.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogUrlExtractor {

    String extract(HttpServletRequest servletRequest);
}
