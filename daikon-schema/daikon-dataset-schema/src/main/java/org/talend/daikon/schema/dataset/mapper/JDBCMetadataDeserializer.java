package org.talend.daikon.schema.dataset.mapper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

public class JDBCMetadataDeserializer extends BuilderBasedDeserializer {

    public static final String NULL_VALUE_HANDLE_AS_ORIGINAL_FIELD_METADATA = "originalFieldMetadata";

    public JDBCMetadataDeserializer(BuilderBasedDeserializer src) {
        super(src);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (NULL_VALUE_HANDLE_AS_ORIGINAL_FIELD_METADATA.equalsIgnoreCase(p.getCurrentName())) {
            return handleNullToken(p, ctxt);
        }

        return super.deserialize(p, ctxt);
    }

    private static Object handleNullToken(JsonParser p, DeserializationContext ctxt) throws IOException {
        String result = StringDeserializer.instance.deserialize(p, ctxt);
        return result != null && result.toLowerCase().equals(null + "") ? null : ctxt.readValue(p, Object.class);
    }
}
