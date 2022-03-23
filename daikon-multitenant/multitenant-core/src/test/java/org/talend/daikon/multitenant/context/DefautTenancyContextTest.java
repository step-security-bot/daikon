package org.talend.daikon.multitenant.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talend.daikon.multitenant.core.Tenant;
import org.talend.daikon.multitenant.provider.DefaultTenant;

public class DefautTenancyContextTest {

    private DefaultTenancyContext tenancyContext;

    @BeforeEach
    public void setUp() {
        this.tenancyContext = new DefaultTenancyContext();
    }

    @Test
    public void testContext() {
        Tenant tenant = new DefaultTenant();
        tenancyContext.setTenant(tenant);
        assertEquals(tenant, tenancyContext.getTenant());
        assertEquals(tenant, tenancyContext.getOptionalTenant().get());
    }

    @Test
    public void testNullContext() {
        assertThrows(NoCurrentTenantException.class, () -> {
            tenancyContext.setTenant(null);
            tenancyContext.getTenant();
        });
    }

    @Test
    public void testOptionalContext() {
        tenancyContext.setTenant(null);
        assertFalse(tenancyContext.getOptionalTenant().isPresent());
    }
}
