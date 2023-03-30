package org.talend.daikon.serialize.jsonschema;

import org.junit.jupiter.api.Test;
import org.talend.daikon.serialize.FullExampleProperties;
import org.talend.daikon.serialize.FullExampleTestUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonPropertiesResolverTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void resolveJson() throws Exception {
        String jsonDataStr = JsonSchemaUtilTest.readJson("FullExampleJsonData.json");
        JsonPropertiesResolver resolver = new JsonPropertiesResolver();
        FullExampleProperties properties = (FullExampleProperties) resolver.resolveJson((ObjectNode) mapper.readTree(jsonDataStr),
                new FullExampleProperties("fullexample").init());

        FullExampleTestUtil.assertPropertiesValueAreEquals(FullExampleTestUtil.createASetupFullExampleProperties(), properties);
    }

}
