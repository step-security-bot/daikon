package org.talend.daikon.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

public class DatasetSchemaValidatorTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static SchemaValidatorsConfig config;

    @BeforeAll
    public static void beforeAll() {
        config = new SchemaValidatorsConfig();
        config.setTypeLoose(false);

        // define internal URI
        Map<String, String> uriMappings = new HashMap<>();
        uriMappings.put("https://org.talend.daikon/dataset.field.schema.json", "resource:/dataset/datasetFieldSchema.json");
        uriMappings.put("https://org.talend.daikon/dataset.field.type.schema.json",
                "resource:/dataset/datasetFieldTypeSchema.json");
        config.setUriMappings(uriMappings);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/fields/datasetField_valid.json", "/fields/datasetField_withExtraProperties_valid.json",
            "/fields/datasetField_hierarchical_valid.json" })
    public void givenValidDatasetFieldInput_whenValidating_thenValid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset/datasetFieldSchema.json"), config);

        JsonNode data = objectMapper.readTree(DatasetSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        for (ValidationMessage validationMessage : result) {
            System.out.println("validationMessage = " + validationMessage);
        }

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/dataset_valid.json", "/dataset_valid2.json" })
    public void givenValidDatasetInput_whenValidating_thenValid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset/datasetSchema.json"), config);

        JsonNode data = objectMapper.readTree(DatasetSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        for (ValidationMessage validationMessage : result) {
            System.out.println("validationMessage = " + validationMessage);
        }

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/dataset_two_types_invalid.json" })
    public void givenInvalidDatasetInput_whenValidating_thenInvalid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(DatasetSchemaValidatorTest.class.getResourceAsStream("/dataset/datasetSchema.json"), config);

        JsonNode data = objectMapper.readTree(DatasetSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        assertTrue(result.size() > 0);
    }

}
