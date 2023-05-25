package org.talend.daikon.schema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.talend.daikon.schema.dataset.DatasetSchema;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializerSerializerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = { "/dataset_valid.json", "/dataset_valid2.json", "/dataset_datetime.json",
            "/dataset_valid_JDBC_partial_metadata.json" })
    public void givenADatasetSchema_whenDeserializeAndSerialize_thenContentIsTheSame(String file)
            throws IOException, JSONException {

        String expected = IOUtils.toString(Objects.requireNonNull(DeserializerSerializerTest.class.getResourceAsStream(file)),
                StandardCharsets.UTF_8);

        DatasetSchema data = OBJECT_MAPPER.readValue(DeserializerSerializerTest.class.getResourceAsStream(file),
                DatasetSchema.class);

        String result = OBJECT_MAPPER.writeValueAsString(data);
        JSONAssert.assertEquals(expected, result, false);
    }
}
