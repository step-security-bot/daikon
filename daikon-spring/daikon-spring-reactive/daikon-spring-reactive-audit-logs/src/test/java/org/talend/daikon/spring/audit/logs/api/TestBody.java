package org.talend.daikon.spring.audit.logs.api;

import lombok.*;

import java.time.ZonedDateTime;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestBody {

    private String name;

    private String password;

    private ZonedDateTime date;

}
