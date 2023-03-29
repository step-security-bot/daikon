package org.talend.daikon.schema.dataset.metadata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = JDBCMetadata.class, visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = JDBCMetadata.class, name = "jdbcMetadata") })
@Getter
@SuperBuilder(toBuilder = true)
public class AbstractOriginalFieldMetadata {
}
