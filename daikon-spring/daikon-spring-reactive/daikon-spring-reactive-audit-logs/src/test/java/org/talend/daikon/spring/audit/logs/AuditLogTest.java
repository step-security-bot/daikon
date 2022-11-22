package org.talend.daikon.spring.audit.logs;

import io.restassured.module.webtestclient.response.WebTestClientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.talend.daikon.multitenant.provider.DefaultTenant;
import org.talend.daikon.security.tenant.ReactiveTenancyContextHolder;
import org.talend.daikon.spring.audit.logs.api.AppTestConfig;
import org.talend.daikon.spring.audit.logs.api.TestBody;
import org.talend.logging.audit.impl.AuditLoggerBase;
import org.talend.logging.audit.impl.DefaultContextImpl;

import java.time.ZonedDateTime;
import java.util.UUID;

import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.talend.daikon.spring.audit.common.model.AuditLogFieldEnum.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AppTestConfig.class)
@AutoConfigureWebTestClient(timeout = "36000")
public class AuditLogTest {

    public static final String TENANT_ID = "ba118361-cf64-47dc-8c72-0fb872a6dc62";

    @Captor
    ArgumentCaptor<DefaultContextImpl> contextArgumentCaptor;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AuditLoggerBase auditLoggerBase;

    private TestBody testBody;

    @BeforeEach
    public void setup() {
        reset(auditLoggerBase);
        ReactiveTenancyContextHolder.withTenant(new DefaultTenant(UUID.randomUUID().toString(), null));
        testBody = TestBody.builder().name("request").date(ZonedDateTime.now()).password("secret").build();
    }

    @Test
    @WithMockUser(username = TENANT_ID)
    public void testAuditLog() {

        doNothing().when(auditLoggerBase).log(any(), any(), contextArgumentCaptor.capture(), any(), any());

        WebTestClientResponse r = given().webTestClient(webTestClient).header(CLIENT_IP.name(), "0.0.0.0.0")
                .contentType(MediaType.APPLICATION_JSON_VALUE).body(testBody).post("/test");

        DefaultContextImpl context = contextArgumentCaptor.getValue();
        assertNotNull(context.get(LOG_ID.getId()));
        assertNotNull(context.get(TIMESTAMP.getId()));
        assertNotNull(context.get(REQUEST_ID.getId()));
        assertNotNull(context.get(CLIENT_IP.getId()));
        assertEquals(AuditLogTestApp.APP_NAME, context.get(APPLICATION_ID.getId()));
        assertEquals(AuditLogTestApp.EVENT_TYPE, context.get(EVENT_TYPE.getId()));
        assertEquals(AuditLogTestApp.EVENT_CATEGORY, context.get(EVENT_CATEGORY.getId()));
        assertEquals("create", context.get(EVENT_OPERATION.getId()));
        assertEquals(TENANT_ID, context.get(ACCOUNT_ID.getId()));
        assertNotNull(context.get(REQUEST.getId()));
        assertNotNull(context.get(RESPONSE.getId()));

        assertFalse(context.get(REQUEST.getId()).contains("secret"));
        assertFalse(context.get(RESPONSE.getId()).contains("secret"));

        assertEquals(OK.value(), r.getStatusCode());

        Mockito.verify(auditLoggerBase).log(any(), any(), any(), any(), any());

    }

    @Test
    @WithMockUser(username = TENANT_ID)
    public void testAuditLogOnError() {

        doNothing().when(auditLoggerBase).log(any(), any(), contextArgumentCaptor.capture(), any(), any());

        WebTestClientResponse r = given().webTestClient(webTestClient).header(CLIENT_IP.name(), "0.0.0.0.0")
                .contentType(MediaType.APPLICATION_JSON_VALUE).body(testBody).put("/test");

        DefaultContextImpl context = contextArgumentCaptor.getValue();
        assertNotNull(context.get(LOG_ID.getId()));
        assertNotNull(context.get(TIMESTAMP.getId()));
        assertNotNull(context.get(REQUEST_ID.getId()));
        assertNotNull(context.get(CLIENT_IP.getId()));
        assertEquals(AuditLogTestApp.APP_NAME, context.get(APPLICATION_ID.getId()));
        assertEquals(AuditLogTestApp.EVENT_TYPE, context.get(EVENT_TYPE.getId()));
        assertEquals(AuditLogTestApp.EVENT_CATEGORY, context.get(EVENT_CATEGORY.getId()));
        assertEquals("update", context.get(EVENT_OPERATION.getId()));
        assertEquals(TENANT_ID, context.get(ACCOUNT_ID.getId()));
        assertNotNull(context.get(REQUEST.getId()));
        assertNotNull(context.get(RESPONSE.getId()));

        assertEquals(INTERNAL_SERVER_ERROR.value(), r.getStatusCode());

        Mockito.verify(auditLoggerBase).log(any(), any(), any(), any(), any());

    }

}
