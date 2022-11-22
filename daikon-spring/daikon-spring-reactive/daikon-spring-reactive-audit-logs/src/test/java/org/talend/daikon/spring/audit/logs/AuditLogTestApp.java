package org.talend.daikon.spring.audit.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.daikon.spring.audit.logs.api.TestBody;
import org.talend.daikon.spring.audit.logs.api.TestFilter;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value = "/test")
@EnableAutoConfiguration
@SpringBootConfiguration
public class AuditLogTestApp {

    public static final String APP_NAME = "test-audit";

    public static final String EVENT_TYPE = "audit";

    public static final String EVENT_CATEGORY = "test";

    public static void main(String[] args) {
        SpringApplication.run(AuditLogTestApp.class, args);
    }

    @ResponseStatus(OK)
    @PostMapping
    @GenerateAuditLog(application = APP_NAME, eventType = EVENT_TYPE, eventCategory = EVENT_CATEGORY, eventOperation = "create", filter = TestFilter.class)
    public Mono<TestBody> create(@RequestBody final TestBody request) {
        return Mono.just(TestBody.builder().name("reponse").password("secret").build());
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @PutMapping
    @GenerateAuditLog(application = APP_NAME, eventType = EVENT_TYPE, eventCategory = EVENT_CATEGORY, eventOperation = "update", filter = TestFilter.class)
    public Mono<TestBody> update(@RequestBody final TestBody request) {
        return Mono.error(new RuntimeException("error"));
    }

}
