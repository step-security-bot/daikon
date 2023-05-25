package org.talend.daikon.schema.dataset.mapper;

import java.io.IOException;
import java.util.List;

import org.talend.daikon.schema.dataset.type.AbstractDatasetFieldTypeSchema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes a list of {@link AbstractDatasetFieldTypeSchema}, this needs a custom implementation because of
 * <a href="https://github.com/FasterXML/jackson-databind/issues/1654#issuecomment-307853268">Jackson specificities</a>.
 * <p>
 * What this class allows is seamless serialization of:
 * <ul>
 * <li>The value null as "null"</li>
 * <li>A single element as itself and not a list containing itself</li>
 * <li>The types as any sub-type of {@link AbstractDatasetFieldTypeSchema} (polymorphism)</li>
 * </ul>
 */
public class SchemaTypesSerializer extends JsonSerializer<List<AbstractDatasetFieldTypeSchema>> {

    @Override
    public void serialize(final List<AbstractDatasetFieldTypeSchema> items, final JsonGenerator jsonGenerator,
            final SerializerProvider serializers) throws IOException {

        if (items == null) {
            jsonGenerator.writeNull();
        } else if (items.size() == 1) {
            writeItem(jsonGenerator, items.get(0));
        } else {
            jsonGenerator.writeStartArray();
            for (AbstractDatasetFieldTypeSchema item : items) {
                writeItem(jsonGenerator, item);
            }
            jsonGenerator.writeEndArray();
        }
    }

    private void writeItem(final JsonGenerator jsonGenerator, final AbstractDatasetFieldTypeSchema item) throws IOException {
        if (item == null) {
            jsonGenerator.writeString("null");
        } else {
            jsonGenerator.writeObject(item);
        }
    }
}
