package org.talend.daikon.schema.dataset.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("jdbcMetadata")
public class JDBCMetadata extends AbstractOriginalFieldMetadata {

    String type;

    Integer size;

    Integer scale;

    Boolean key;

    Boolean foreignKey;

    Boolean unique;
}
