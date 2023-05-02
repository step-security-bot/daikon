package org.talend.daikon.schema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.daikon.schema.dataset.DatasetSchema;
import org.talend.daikon.schema.dataset.mapper.DatasetSchemaMapperConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializerSerializerTest {

    private static ObjectMapper objectMapper = DatasetSchemaMapperConfiguration.datasetSchemaObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = { "/dataset_valid.json", "/dataset_valid2.json", "/dataset_datetime.json",
            "/dataset_valid_JDBC_partial_metadata.json" })
    public void givenADatasetSchema_whenDeserializeAndSerialize_thenContentIsTheSame(String file)
            throws IOException, JSONException {

        String expected = IOUtils.toString(DeserializerSerializerTest.class.getResourceAsStream(file),
                StandardCharsets.UTF_8.name());

        DatasetSchema data = objectMapper.readValue(DeserializerSerializerTest.class.getResourceAsStream(file),
                DatasetSchema.class);

        String result = objectMapper.writeValueAsString(data);
        JSONAssert.assertEquals(expected, result, false);
    }
}
