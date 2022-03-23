// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.exception;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ExceptionContext}
 */
public class ExceptionContextTest {

    @Test
    public void createExceptionContextWithBuilder() {
        ExceptionContext context = ExceptionContext.withBuilder().put("key1", "value1").put("key2", "value2")
                .put("key3", "value3").build();
        assertTrue(context.contains("key1"));
        assertTrue(context.contains("key2"));
        assertTrue(context.contains("key3"));
    }

}
