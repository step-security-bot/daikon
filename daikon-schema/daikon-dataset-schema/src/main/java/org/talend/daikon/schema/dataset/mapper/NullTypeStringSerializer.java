package org.talend.daikon.schema.dataset.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class NullTypeStringSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object action, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(null + "");
    }
}
