// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.spring.audit.logs.api;

import org.talend.daikon.spring.audit.logs.service.AuditLogContextBuilder;

public class NoOpAuditContextFilter implements AuditContextFilter {

    @Override
    public AuditLogContextBuilder filter(AuditLogContextBuilder auditLogContextBuilder, String requestBody, String responseBody) {
        return auditLogContextBuilder;
    }
}
