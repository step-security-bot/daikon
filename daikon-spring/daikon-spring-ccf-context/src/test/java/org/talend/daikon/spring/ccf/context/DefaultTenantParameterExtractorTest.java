package org.talend.daikon.spring.ccf.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultTenantParameterExtractorTest {

    private static final String PATH_WITH_TENANT = "/my/service/tenant/tenantId/resource/resourceId";

    private static final String PATH_WITHOUT_TENANT = "/my/service/resource/resourceId";

    private TenantParameterExtractor tenantParameterExtractor = new TenantParameterExtractorImpl();

    @Test
    void whenTenantIdNotInPathReturnNull() {
        String tenantId = tenantParameterExtractor.extractParam(PATH_WITHOUT_TENANT, "tenant");
        Assertions.assertNull(tenantId);
    }

    @Test
    void whenTenantIdInPathReturnTenantId() {
        String tenantId = tenantParameterExtractor.extractParam(PATH_WITH_TENANT, "tenant");
        Assertions.assertEquals("tenantId", tenantId);

    }
}
