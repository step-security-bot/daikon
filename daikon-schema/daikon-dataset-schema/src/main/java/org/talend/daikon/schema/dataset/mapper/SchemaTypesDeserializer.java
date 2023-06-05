package org.talend.daikon.schema.dataset.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.daikon.schema.dataset.type.AbstractDatasetFieldTypeSchema;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * De-serializes a list of {@link AbstractDatasetFieldTypeSchema}, this needs a custom implementation because of
 * <a href="https://github.com/FasterXML/jackson-databind/issues/1654#issuecomment-307853268">Jackson specificities</a>.
 * <p>
 * What this class allows is seamless de-serialization of:
 * <ul>
 * <li>The value "null" as null</li>
 * <li>A single element as a list containing only itself</li>
 * </ul>
 */
public class SchemaTypesDeserializer extends JsonDeserializer<List<AbstractDatasetFieldTypeSchema>> {

    private static final ObjectMapper internalMapper = new ObjectMapper();

    @Override
    public List<AbstractDatasetFieldTypeSchema> deserialize(final JsonParser jsonParser,
            final DeserializationContext deserializationContext) throws IOException {
        final ObjectCodec codec = jsonParser.getCodec();
        final JsonNode node = codec.readTree(jsonParser);

        if (node.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) node;
            final ArrayList<AbstractDatasetFieldTypeSchema> result = new ArrayList<>(arrayNode.size());
            for (final JsonNode jsonNode : arrayNode) {
                result.add(deserializeItem(jsonNode));
            }
            return result;
        }

        return Collections.singletonList(deserializeItem(node));
    }

    private AbstractDatasetFieldTypeSchema deserializeItem(final JsonNode item) throws JsonProcessingException {
        if (item.isObject()) {
            return internalMapper.treeToValue(item, AbstractDatasetFieldTypeSchema.class);
        } else if (item.isTextual() && item.asText().equals("null")) {
            return null;
        }

        throw new RuntimeException(String.format("Cannot de-serialize %s as %s", item.asText(),
                AbstractDatasetFieldTypeSchema.class.getCanonicalName()));
    }
}
