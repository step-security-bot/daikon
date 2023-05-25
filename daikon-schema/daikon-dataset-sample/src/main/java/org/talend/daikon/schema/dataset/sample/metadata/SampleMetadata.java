package org.talend.daikon.schema.dataset.sample.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link sample/sampleMetadata.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleMetadata {

    List<RecordField> fields;
}
