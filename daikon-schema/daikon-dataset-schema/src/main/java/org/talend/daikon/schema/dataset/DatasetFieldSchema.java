package org.talend.daikon.schema.dataset;

import java.util.List;
import java.util.Map;

import org.talend.daikon.schema.dataset.mapper.NullTypeStringSerializer;
import org.talend.daikon.schema.dataset.metadata.AbstractOriginalFieldMetadata;
import org.talend.daikon.schema.dataset.type.AbstractDatasetFieldTypeSchema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link dataset/datasetFieldSchema.json}
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFieldSchema {

    String name;

    @JsonSerialize(nullsUsing = NullTypeStringSerializer.class)
    @JsonFormat(with = JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    List<AbstractDatasetFieldTypeSchema> type;

    String originalFieldName;

    String description;

    @JsonAnySetter
    @Singular
    Map<String, Object> additionalProperties;

    AbstractOriginalFieldMetadata originalFieldMetadata;

    @JsonAnyGetter
    @JsonUnwrapped
    // workaround in order to be able to unwrapped @JsonAnySetter field
    public Map getAdditionalProperties() {
        return additionalProperties;
    }
}
