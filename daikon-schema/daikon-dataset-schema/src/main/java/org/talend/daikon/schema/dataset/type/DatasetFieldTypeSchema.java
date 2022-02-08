package org.talend.daikon.schema.dataset.type;

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
public class DatasetFieldTypeSchema extends AbstractDatasetFieldTypeSchema {

}
