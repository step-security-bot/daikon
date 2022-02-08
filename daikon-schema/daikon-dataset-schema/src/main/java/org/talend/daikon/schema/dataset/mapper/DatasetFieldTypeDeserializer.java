package org.talend.daikon.schema.dataset.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

public class DatasetFieldTypeDeserializer extends BuilderBasedDeserializer {

    public DatasetFieldTypeDeserializer(BuilderBasedDeserializer src) {
        super(src);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // default deserializer use case
        if (p.getCurrentTokenId() == JsonTokenId.ID_FIELD_NAME || p.isExpectedStartArrayToken()
                || p.isExpectedStartObjectToken()) {
            return super.deserialize(p, ctxt);
        }
        // handle "null" token
        String result = StringDeserializer.instance.deserialize(p, ctxt);
        return result != null && result.toLowerCase().equals(null + "") ? null : ctxt.readValue(p, Object.class);

    }

}
