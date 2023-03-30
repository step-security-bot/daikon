package org.talend.daikon.spring.audit.logs.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogIpExtractor {

    String extract(HttpServletRequest servletRequest);
}
