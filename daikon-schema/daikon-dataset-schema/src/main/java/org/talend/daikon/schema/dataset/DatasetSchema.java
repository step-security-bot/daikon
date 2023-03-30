package org.talend.daikon.schema.dataset;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link dataset/datasetSchema.json}
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetSchema {

    String name;

    String type;

    String namespace;

    List<DatasetFieldSchema> fields;

    @JsonAnySetter
    @Singular
    Map<String, Object> additionalProperties;

    @JsonAnyGetter
    @JsonUnwrapped
    // workaround in order to be able to unwrapped @JsonAnySetter field
    public Map getAdditionalProperties() {
        return additionalProperties;
    }

}