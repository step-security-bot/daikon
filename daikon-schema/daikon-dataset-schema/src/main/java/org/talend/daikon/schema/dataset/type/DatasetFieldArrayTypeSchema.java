package org.talend.daikon.schema.dataset.type;

import java.util.List;

import org.talend.daikon.schema.dataset.DatasetFieldSchema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Pojo representation of {@link dataset/datasetFieldTypeSchema.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFieldArrayTypeSchema extends AbstractDatasetFieldTypeSchema {

    DatasetFieldTypeSchema items;

    @JsonIgnore
    public String getLogicalType() {
        return items.getLogicalType();
    }

    @JsonIgnore
    public String getNamespace() {
        return items.getNamespace();
    }

    @JsonIgnore
    public String getDqType() {
        return items.getDqType();
    }

    @JsonIgnore
    public String getDqTypeId() {
        return items.getDqTypeId();
    }

    @JsonIgnore
    public String getDqNativeType() {
        return items.getDqNativeType();
    }

    @JsonIgnore
    public Boolean getForced() {
        return items.getForced();
    }

    @JsonIgnore
    public Boolean getDatetime() {
        return items.getDatetime();
    }

    @JsonIgnore
    public Boolean getNativeForced() {
        return items.getNativeForced();
    }

    @JsonIgnore
    public List<DatasetFieldSchema> getFields() {
        return items.getFields();
    }

}
