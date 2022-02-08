package org.talend.daikon.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.daikon.schema.dataset.mapper.DatasetFieldTypeDeserializer;
import org.talend.daikon.schema.dataset.mapper.NullTypeStringSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DeserializerSerializeNullTest {

    private static ObjectMapper objectMapper;

    {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == NullValuePojo.SubNullValuePojo.SubNullValuePojoBuilder.class) {
                    return new DatasetFieldTypeDeserializer((BuilderBasedDeserializer) deserializer);
                }
                return deserializer;
            }
        });
        module.setSerializerModifier(new BeanSerializerModifier() {

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                    JsonSerializer<?> serializer) {
                return serializer;
            }
        });
        objectMapper.getSerializerProvider().setNullValueSerializer(new NullTypeStringSerializer());
        objectMapper.registerModule(module);
    }

    @Test
    public void givenNullValue_whenDeserialize_thenNoError() throws IOException {

        NullValuePojo data = objectMapper.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/nullValueDeserialization.json"), NullValuePojo.class);

        assertNotNull(data);
        assertEquals(3, data.getNumber().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/nullValueDeserialization.json" })
    public void givenADatasetSchema_whenDeserializeAndSerialize_thenContentIsTheSame(String file)
            throws IOException, JSONException {

        String expected = IOUtils.toString(DeserializerSerializerTest.class.getResourceAsStream(file),
                StandardCharsets.UTF_8.name());

        NullValuePojo data = objectMapper.readValue(DeserializerSerializerTest.class.getResourceAsStream(file),
                NullValuePojo.class);

        String result = objectMapper.writeValueAsString(data);

        JSONAssert.assertEquals(expected, result, false);
    }
}
