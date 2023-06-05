package org.talend.daikon.schema.dataset.sample.metadata;

import java.util.List;

/**
 * Pojo representation of {@link sample/recordField.json}
 */
public record RecordField(String name, RecordFieldQuality quality, List<RecordField> fields) {
}
