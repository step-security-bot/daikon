package org.talend.daikon.schema.dataset.sample.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link sample/recordFieldQuality.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordFieldQuality {

    QualityStatus aggregated;

    QualityStatus dqType;

    List<DqRuleQuality> dqRules;
}
