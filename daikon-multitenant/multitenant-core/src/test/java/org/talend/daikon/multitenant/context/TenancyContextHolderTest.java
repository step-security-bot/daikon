// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.multitenant.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.daikon.multitenant.core.Tenant;
import org.talend.daikon.multitenant.provider.DefaultTenant;

public class TenancyContextHolderTest {

    @BeforeEach
    public void setUp() throws Exception {
        TenancyContextHolder.setStrategyName(TenancyContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @AfterEach
    public void tearDown() {
        TenancyContextHolder.setStrategyName(TenancyContextHolder.MODE_THREADLOCAL);
    }

    @Test
    public void testContextHolderGetterSetterClearer() {
        TenancyContext tc = new DefaultTenancyContext();
        tc.setTenant(new DefaultTenant("id", "myTenant"));
        TenancyContextHolder.setContext(tc);
        assertEquals(tc, TenancyContextHolder.getContext());
        TenancyContextHolder.clearContext();
        assertNotSame(tc, TenancyContextHolder.getContext());
        TenancyContextHolder.clearContext();
    }

    @Test
    public void testNeverReturnsNull() {
        assertNotNull(TenancyContextHolder.getContext());
        TenancyContextHolder.clearContext();
    }

    @Test
    public void testRejectsNulls() {
        try {
            TenancyContextHolder.setContext(null);
            fail("Should have rejected null");
        } catch (IllegalArgumentException expected) {

        }
    }

    @Test
    public void testOptionalContext() {
        TenancyContextHolder.setContext(TenancyContextHolder.createEmptyContext());
        Optional<Tenant> optionalTenant = TenancyContextHolder.getContext().getOptionalTenant();
        assertFalse(optionalTenant.isPresent());
    }

    @Test
    public void testNullContext() {
        assertThrows(NoCurrentTenantException.class, () -> {
            TenancyContextHolder.setContext(TenancyContextHolder.createEmptyContext());
            TenancyContextHolder.getContext().getTenant();
        });
    }

    static class StatusHolder {

        private Boolean success;

        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public void assertEquals(Object expected, Object result) {
            if (alreadyFailed()) {
                return;
            }
            success = expected == result;
            addMessageIfFailed("'" + result + "' should be equals to expected value: '" + expected + "'");
        }

        public void assertNotEquals(Object expected, Object result) {
            if (alreadyFailed()) {
                return;
            }
            success = expected != result;
            addMessageIfFailed("'" + result + "' should be different from expected value: '" + expected + "'");
        }

        public void assertNull(Object expected, Object result) {
            if (alreadyFailed()) {
                return;
            }
            success = expected != result;
            addMessageIfFailed("'" + result + "' should be different from expected value: '" + expected + "'");
        }

        public void assertTrue(String message, boolean expression) {
            if (alreadyFailed()) {
                return;
            }
            success = expression;
            if (!success) {
                this.message = message;
            }
        }

        public void addMessageIfFailed(String message) {
            if (!success) {
                this.message = message;
            }
        }

        public void assertSuccess() {
            assertTrue(message, success);
        }

        private boolean alreadyFailed() {
            return success != null && !success;
        }
    }
}
