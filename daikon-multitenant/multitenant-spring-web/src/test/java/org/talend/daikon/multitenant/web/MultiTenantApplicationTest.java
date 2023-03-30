// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.multitenant.web;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.talend.daikon.multitenant.web.MultiTenantApplication.MESSAGE;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.talend.daikon.logging.event.field.MdcKeys;
import org.talend.daikon.multitenant.context.TenancyContextHolder;

import io.restassured.RestAssured;

@ExtendWith(SpringExtension.class)
@Import(MultiTenantApplicationTest.SampleRequestHandlerConfiguration.class)
@SpringBootTest(classes = { MultiTenantApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultiTenantApplicationTest {

    @Value("${local.server.port}")
    public int port;

    @Autowired
    private SampleRequestHandlerConfiguration handlerConfiguration;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        handlerConfiguration.verifier = () -> {
        };
    }

    @Test
    public void testSyncWithoutTenant() {
        handlerConfiguration.verifier = () -> {
            assertFalse(TenancyContextHolder.getContext().getOptionalTenant().isPresent());
            assertNull(MDC.get(MdcKeys.ACCOUNT_ID));
        };
        given().get("/sync").then().statusCode(200).body(Matchers.equalTo(MESSAGE));
    }

    @Test
    public void testSyncWithTenantHeader() {
        String tenantId = "MyTestTenantId";
        handlerConfiguration.verifier = () -> {
            assertEquals(tenantId, TenancyContextHolder.getContext().getTenant().getIdentity());
            assertEquals(tenantId, MDC.get(MdcKeys.ACCOUNT_ID));
        };
        given().log().all().header(MultiTenantApplication.TENANT_HTTP_HEADER, tenantId).get("/sync").then().log().all()
                .statusCode(200).body(Matchers.equalTo(MESSAGE));
    }

    @Test
    public void testAsyncWithoutTenant() {
        handlerConfiguration.verifier = () -> {
            assertFalse(TenancyContextHolder.getContext().getOptionalTenant().isPresent());
            assertNull(MDC.get(MdcKeys.ACCOUNT_ID));
        };
        given().get("/async").then().statusCode(200).body(Matchers.equalTo(MESSAGE));
    }

    @Test
    public void testAsyncWithTenantHeader() {
        String tenantId = "MyAsyncTestTenantId";
        handlerConfiguration.verifier = () -> {
            assertEquals(tenantId, TenancyContextHolder.getContext().getTenant().getIdentity());
            assertEquals(tenantId, MDC.get(MdcKeys.ACCOUNT_ID));
        };
        given().header(MultiTenantApplication.TENANT_HTTP_HEADER, tenantId).get("/async").then().statusCode(200)
                .body(Matchers.equalTo(MESSAGE));
    }

    @Test
    public void testSyncWithTenantAndError() {
        String errorMessage = "Expected error message";
        String tenantId = "MyTestTenantId";
        handlerConfiguration.verifier = () -> {
            assertEquals(tenantId, TenancyContextHolder.getContext().getTenant().getIdentity());
            assertEquals(tenantId, MDC.get(MdcKeys.ACCOUNT_ID));
            throw new RuntimeException(errorMessage);
        };
        given().header(MultiTenantApplication.TENANT_HTTP_HEADER, tenantId).get("/sync").then().statusCode(500);
    }

    @Test
    public void testAsyncWithTenantAndError() {
        String errorMessage = "Expected error message";
        String tenantId = "MyTestTenantId";
        handlerConfiguration.verifier = () -> {
            assertEquals(tenantId, TenancyContextHolder.getContext().getTenant().getIdentity());
            assertEquals(tenantId, MDC.get(MdcKeys.ACCOUNT_ID));
            throw new RuntimeException(errorMessage);
        };
        given().header(MultiTenantApplication.TENANT_HTTP_HEADER, tenantId).get("/async").then().statusCode(500);
    }

    @AutoConfiguration
    public static class SampleRequestHandlerConfiguration {

        public Runnable verifier = () -> {
        };

        @Bean
        public MultiTenantApplication.SampleRequestHandler sampleRequestHandler() {
            return () -> verifier.run();
        }
    }

}
