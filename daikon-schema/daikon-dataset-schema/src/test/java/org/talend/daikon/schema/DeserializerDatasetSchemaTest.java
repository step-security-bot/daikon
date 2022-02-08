package org.talend.daikon.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.talend.daikon.schema.dataset.DatasetSchema;
import org.talend.daikon.schema.dataset.mapper.DatasetSchemaMapperConfiguration;

import java.io.IOException;

public class DeserializerDatasetSchemaTest {

    private static ObjectMapper objectMapper = DatasetSchemaMapperConfiguration.datasetSchemaObjectMapper();

    @Test
    public void givenValidDatasetInput_whenDeserialize_thenNoError() throws IOException {
        DatasetSchema data = objectMapper.readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid.json"),
                DatasetSchema.class);

        assertNotNull(data);
        assertEquals("record", data.getType());
        assertEquals("Person", data.getName());
    }

    @Test
    public void givenValidDatasetInput2_whenDeserialize_thenNoError() throws IOException {

        DatasetSchema data = objectMapper.readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid2.json"),
                DatasetSchema.class);

        assertNotNull(data);
    }

    @Test
    public void givenValidDatasetInputWithDatetimeField_whenDeserialize_thenNoError() throws IOException {

        DatasetSchema data = objectMapper
                .readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_datetime.json"), DatasetSchema.class);

        assertNotNull(data);
        assertTrue((data.getFields().get(8).getType().get(1)).getDatetime());

    }
}
