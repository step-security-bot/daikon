package org.talend.daikon.spring.audit.logs.api;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

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
