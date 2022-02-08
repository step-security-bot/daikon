package org.talend.daikon.schema.dataset.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.talend.daikon.schema.dataset.DatasetFieldSchema;

import java.util.List;

/**
 * Pojo representation of {@link dataset/datasetFieldTypeSchema.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFieldMapTypeSchema extends AbstractDatasetFieldTypeSchema {

    DatasetFieldTypeSchema values;

    @JsonIgnore
    public String getLogicalType() {
        return values.getLogicalType();
    }

    @JsonIgnore
    public String getNamespace() {
        return values.getNamespace();
    }

    @JsonIgnore
    public String getDqType() {
        return values.getDqType();
    }

    @JsonIgnore
    public String getDqTypeId() {
        return values.getDqTypeId();
    }

    @JsonIgnore
    public String getDqNativeType() {
        return values.getDqNativeType();
    }

    @JsonIgnore
    public Boolean getForced() {
        return values.getForced();
    }

    @JsonIgnore
    public Boolean getDatetime() {
        return values.getDatetime();
    }

    @JsonIgnore
    public Boolean getNativeForced() {
        return values.getNativeForced();
    }

    @JsonIgnore
    public List<DatasetFieldSchema> getFields() {
        return values.getFields();
    }

}
