package org.talend.daikon.schema.dataset.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

import org.talend.daikon.schema.dataset.mapper.SchemaTypesDeserializer;
import org.talend.daikon.schema.dataset.mapper.SchemaTypesSerializer;

/**
 * Pojo representation of {@link dataset/datasetFieldTypeSchema.json}
 */
@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetFieldArrayTypeSchema extends AbstractDatasetFieldTypeSchema {

    @JsonSerialize(using = SchemaTypesSerializer.class)
    @JsonDeserialize(using = SchemaTypesDeserializer.class)
    List<DatasetFieldTypeSchema> items;

}
