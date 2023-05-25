package org.talend.daikon.schema.dataset.sample.metadata;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link sample/recordField.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecordField {

    String name;

    RecordFieldQuality quality;

    List<RecordField> fields;
}
