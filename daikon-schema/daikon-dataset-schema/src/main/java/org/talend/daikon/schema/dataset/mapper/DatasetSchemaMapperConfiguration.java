package org.talend.daikon.schema.dataset.mapper;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.talend.daikon.schema.dataset.type.DatasetFieldTypeSchema;

public class DatasetSchemaMapperConfiguration {

    public static ObjectMapper datasetSchemaObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass().getName()
                        .contains(DatasetFieldTypeSchema.DatasetFieldTypeSchemaBuilder.class.getName())) {
                    return new DatasetFieldTypeDeserializer((BuilderBasedDeserializer) deserializer);
                }
                return deserializer;
            }
        });

        objectMapper.getSerializerProvider().setNullValueSerializer(new NullTypeStringSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
