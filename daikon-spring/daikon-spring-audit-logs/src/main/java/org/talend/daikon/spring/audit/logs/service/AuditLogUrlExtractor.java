package org.talend.daikon.spring.audit.logs.service;

import javax.servlet.http.HttpServletRequest;

public interface AuditLogUrlExtractor {

    String extract(HttpServletRequest servletRequest);
}
