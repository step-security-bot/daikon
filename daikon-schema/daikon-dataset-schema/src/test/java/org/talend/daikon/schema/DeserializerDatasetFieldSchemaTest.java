package org.talend.daikon.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.talend.daikon.schema.dataset.DatasetFieldSchema;
import org.talend.daikon.schema.dataset.type.DatasetFieldArrayTypeSchema;
import org.talend.daikon.schema.dataset.type.DatasetFieldType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializerDatasetFieldSchemaTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void givenValidDatasetFieldInput_whenDeserialize_thenNoError() throws IOException {

        DatasetFieldSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/fields/datasetField_valid.json"),
                DatasetFieldSchema.class);

        assertNotNull(data);
        assertEquals("title", data.getName());
        assertEquals(DatasetFieldType.STRING, data.getType().get(0).getType());
        assertEquals(true, data.getType().get(0).getForced());
        assertEquals(true, data.getType().get(0).getNativeForced());
        assertEquals("my real title", data.getOriginalFieldName());
        assertEquals("my awesome description", data.getDescription());
    }

    @Test
    public void givenValidDatasetFieldInputWithExtraParam_whenDeserialize_thenNoError() throws IOException {

        DatasetFieldSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/fields/datasetField_withExtraProperties_valid.json"),
                DatasetFieldSchema.class);

        assertNotNull(data);
        assertEquals(1, data.getAdditionalProperties().size());
        assertEquals("extraValue1", data.getAdditionalProperties().get("extraParam1"));
        assertEquals(4, data.getType().get(0).getAdditionalProperties().size());
        assertEquals("extraValue2", data.getType().get(0).getAdditionalProperties().get("extraParam2"));
    }

    @Test
    public void givenValidDatasetFieldInputWithNull_whenDeserialize_thenNoError() throws IOException {

        DatasetFieldSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/fields/datasetField_withNull_valid.json"),
                DatasetFieldSchema.class);

        assertNotNull(data);
        assertEquals(2, data.getType().size());
        assertEquals("Title", data.getType().get(1).getDqType());
    }

    @Test
    public void givenValidDatasetFieldInputWithHierarchicalField_whenDeserialize_thenNoError() throws IOException {

        DatasetFieldSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/fields/datasetField_hierarchical_valid.json"),
                DatasetFieldSchema.class);

        assertNotNull(data);
        assertEquals(2, data.getType().get(0).getFields().size());
        assertEquals("firstname", data.getType().get(0).getFields().get(0).getName());
        assertEquals("lastname", data.getType().get(0).getFields().get(1).getName());
        assertEquals("org.talend.dq.processors.a", data.getType().get(0).getNamespace());
    }

    @Test
    public void givenValidDatasetFieldInputWithArrayField_whenDeserialize_thenNoError() throws IOException {

        DatasetFieldSchema data = OBJECT_MAPPER.readValue(
                DatasetSchemaValidatorTest.class.getResourceAsStream("/fields/datasetField_withArray_valid.json"),
                DatasetFieldSchema.class);

        assertNotNull(data);
        assertEquals(DatasetFieldArrayTypeSchema.class, data.getType().get(0).getClass());
        assertEquals(DatasetFieldType.ARRAY, data.getType().get(0).getType());
        assertNull(((DatasetFieldArrayTypeSchema) data.getType().get(0)).getItems().get(0));
        assertEquals(DatasetFieldType.STRING, ((DatasetFieldArrayTypeSchema) data.getType().get(0)).getItems().get(1).getType());
    }

}
