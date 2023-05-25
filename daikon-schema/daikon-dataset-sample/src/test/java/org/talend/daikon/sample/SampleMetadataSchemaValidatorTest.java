package org.talend.daikon.sample;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.talend.daikon.schema.dataset.sample.metadata.DqRuleQuality;
import org.talend.daikon.schema.dataset.sample.metadata.QualityStatus;
import org.talend.daikon.schema.dataset.sample.metadata.RecordField;
import org.talend.daikon.schema.dataset.sample.metadata.RecordFieldQuality;
import org.talend.daikon.schema.dataset.sample.metadata.RuleQualityStatus;
import org.talend.daikon.schema.dataset.sample.metadata.SampleMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SampleMetadataSchemaValidatorTest {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private static SchemaValidatorsConfig config;

    @BeforeAll
    public static void beforeAll() {
        config = new SchemaValidatorsConfig();
        config.setTypeLoose(false);

        // define internal URI
        Map<String, String> uriMappings = new HashMap<>();
        uriMappings.put("https://org.talend.daikon/dqRule.quality.schema.json", "resource:/sample/dqRuleQuality.json");
        uriMappings.put("https://org.talend.daikon/record.field.schema.json", "resource:/sample/recordField.json");
        uriMappings.put("https://org.talend.daikon/record.field.quality.schema.json", "resource:/sample/recordFieldQuality.json");
        uriMappings.put("https://org.talend.daikon/sample.metadata.schema.json", "resource:/sample/sampleMetadata.json");
        config.setUriMappings(uriMappings);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/sampleMetadata/sampleMetadata.json" })
    public void givenValidSampleMetadata_whenValidating_thenValid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(SampleMetadataSchemaValidatorTest.class.getResourceAsStream("/sample/sampleMetadata.json"), config);

        JsonNode data = objectMapper.readTree(SampleMetadataSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        for (ValidationMessage validationMessage : result) {
            System.out.println("validationMessage = " + validationMessage);
        }

        assertEquals(0, result.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/sampleMetadata/sampleMetadata.json" })
    public void givenInvaliValidSampleMetadata_whenValidating_thenInvalid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(SampleMetadataSchemaValidatorTest.class.getResourceAsStream("/sample/sampleMetadata.json"), config);

        JsonNode data = objectMapper.readTree(SampleMetadataSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        for (ValidationMessage validationMessage : result) {
            System.out.println("validationMessage = " + validationMessage);
        }

        assertEquals(0, result.size());
    }

    @Test
    public void givenSampleMetadata_thenSerialize() throws IOException, JSONException {
        var recordFieldQuality = new RecordFieldQuality(QualityStatus.VALID,
                QualityStatus.EMPTY, List.of(new DqRuleQuality("123",
                RuleQualityStatus.VALID)));
        var field = new RecordField("price", recordFieldQuality, List.of());

        var sampleMetadata = new SampleMetadata(List.of(field));
        var expectedSampleMetadata = """
                {
                  "fields": [
                    {
                      "name": "price",
                      "quality": {
                        "aggregated": "VALID",
                        "dqType": "EMPTY",
                        "dqRules": [
                          {
                            "id": "123",
                            "result": "VALID"
                          }
                        ]
                      },
                      "fields": []
                    }
                  ]
                }
                """;
        JSONAssert.assertEquals(expectedSampleMetadata,
                objectMapper.writeValueAsString(sampleMetadata),
                JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void givenValidSampleMetadata_whenDeserialize_thenNoError() throws IOException {

        var data = objectMapper.readValue(
                SampleMetadataSchemaValidatorTest.class.getResourceAsStream("/sampleMetadata/sampleMetadata.json"),
                SampleMetadata.class);

        assertNotNull(data);
        assertEquals(3, data.fields().size());
        assertEquals(QualityStatus.INVALID, data.fields().get(0).quality().aggregated());
        assertEquals(1, data.fields().get(0).quality().dqRules().size());
        assertEquals(RuleQualityStatus.VALID, data.fields().get(0).quality().dqRules().get(0).result());
        assertEquals(1, data.fields().get(2).fields().size());
        assertEquals(QualityStatus.EMPTY, data.fields().get(2).fields().get(0).quality().dqType());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/sampleMetadata/sampleMetadataInvalid.json" })
    public void givenInvaliValidSampleMetadata_whenValidating_thenInValid(String jsonFile) throws IOException {

        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                .getSchema(SampleMetadataSchemaValidatorTest.class.getResourceAsStream("/sample/sampleMetadata.json"), config);

        JsonNode data = objectMapper.readTree(SampleMetadataSchemaValidatorTest.class.getResourceAsStream(jsonFile));
        Set<ValidationMessage> result = jsonSchema.validate(data);

        for (ValidationMessage validationMessage : result) {
            System.out.println("validationMessage = " + validationMessage);
        }

        assertEquals(3, result.size());
    }

}
