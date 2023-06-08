package org.talend.daikon.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.talend.daikon.schema.dataset.DatasetSchema;
import org.talend.daikon.schema.dataset.metadata.JDBCMetadata;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializerDatasetSchemaTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void givenValidDatasetInput_whenDeserialize_thenNoError() throws IOException {
        DatasetSchema data = OBJECT_MAPPER.readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid.json"),
                DatasetSchema.class);

        assertNotNull(data);
        assertEquals("record", data.getType());
        assertEquals("Person", data.getName());
    }

    @Test
    public void givenValidDatasetInput2_whenDeserialize_thenNoError() throws IOException {

        DatasetSchema data = OBJECT_MAPPER.readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid2.json"),
                DatasetSchema.class);

        assertNotNull(data);
    }

    @Test
    public void givenValidDatasetInput3_whenDeserialize_thenNoError() throws IOException {
        DatasetSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid_JDBC_metadata.json"), DatasetSchema.class);

        assertNotNull(data);
        assertEquals("record", data.getType());
        assertEquals("Person", data.getName());
        assertTrue(data.getFields().get(0).getOriginalFieldMetadata() instanceof JDBCMetadata);
        JDBCMetadata jdbcMetadata = (JDBCMetadata) data.getFields().get(0).getOriginalFieldMetadata();
        assertEquals("varchar", jdbcMetadata.getType());
        assertEquals(10, jdbcMetadata.getSize());
        assertEquals(3, jdbcMetadata.getScale());
        assertTrue(jdbcMetadata.getKey());
        assertFalse(jdbcMetadata.getForeignKey());
        assertTrue(jdbcMetadata.getUnique());
    }

    @Test
    public void givenValidDatasetInput4_whenDeserialize_thenNoError() throws IOException {
        DatasetSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid_JDBC_partial_metadata.json"),
                DatasetSchema.class);

        assertNotNull(data);
        assertEquals("record", data.getType());
        assertEquals("Person", data.getName());
        assertTrue(data.getFields().get(0).getOriginalFieldMetadata() instanceof JDBCMetadata);
        JDBCMetadata jdbcMetadata = (JDBCMetadata) data.getFields().get(0).getOriginalFieldMetadata();
        assertEquals("varchar", jdbcMetadata.getType());
        assertEquals(10, jdbcMetadata.getSize());
        assertNull(jdbcMetadata.getScale());
        assertNull(jdbcMetadata.getKey());
        assertNull(jdbcMetadata.getForeignKey());
        assertNull(jdbcMetadata.getUnique());
    }

    @Test
    public void givenValidDatasetInputWithDatetimeField_whenDeserialize_thenNoError() throws IOException {

        DatasetSchema data = OBJECT_MAPPER
                .readValue(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_datetime.json"), DatasetSchema.class);

        assertNotNull(data);
        assertTrue((data.getFields().get(8).getType().get(1)).getDatetime());

    }

    @Test
    public void givenOriginalFieldMetadataAsNull_whenDeserialize_thenNoError() throws IOException {
        DatasetSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset_valid_JDBC_partial_metadata.json"),
                DatasetSchema.class);

        assertNull(data.getFields().get(1).getOriginalFieldMetadata());
    }
}
