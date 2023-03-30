package org.talend.daikon.schema.dataset.type;

import java.util.List;
import java.util.Map;

import org.talend.daikon.schema.dataset.DatasetFieldSchema;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, defaultImpl = DatasetFieldTypeSchema.class, visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = DatasetFieldArrayTypeSchema.class, name = "array"),
        @JsonSubTypes.Type(value = DatasetFieldMapTypeSchema.class, name = "map") })
@Getter
@SuperBuilder(toBuilder = true)
public abstract class AbstractDatasetFieldTypeSchema {

    DatasetFieldType type;

    String logicalType;

    String namespace;

    String dqType;

    String dqTypeId;

    String dqNativeType;

    @JsonProperty("isForced")
    Boolean forced;

    @JsonProperty("isDatetime")
    Boolean datetime;

    @JsonProperty("isNativeForced")
    Boolean nativeForced;

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
