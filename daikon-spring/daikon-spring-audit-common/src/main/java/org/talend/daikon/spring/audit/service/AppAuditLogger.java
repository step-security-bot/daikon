package org.talend.daikon.spring.audit.service;

import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.EventAuditLogger;

public interface AppAuditLogger extends EventAuditLogger {

    @AuditEvent(category = "audit log")
    void sendAuditLog(Object... args);
}
