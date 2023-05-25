package org.talend.daikon.schema.dataset.sample.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link sample/dqRuleQuality.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DqRuleQuality {

    String id;

    RuleQualityStatus result;
}
