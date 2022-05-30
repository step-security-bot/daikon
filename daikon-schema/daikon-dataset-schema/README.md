# Common dataset schema description

The goal of this module is to provide a *common* dataset schema description. 

This work is based on [DQ-Runtime Schema](https://github.com/Talend/data-processing-runtime/blob/main/docs/specification/DQ-Runtime_Schema/design.md).

Explanation about how dataset schema is used across Talend products can be found on [Lucidchart](https://lucid.app/lucidchart/cf1ec35f-db0b-41e2-aebd-86ce8df0d3d2/edit?invitationId=inv_b9f735dd-a269-44b7-b3dd-e1af0bb68017).

## JSON Schema

JSON schemas representing a dataset schema are located under `src/main/resources` folder. They define mandatory and none-mandatory fields and set a description for each field.

## Modification made on DQ-Runtime Schema

Small adjustments have been made to the original DQ-Runtime Schema:

* `dqTypeLabel` is removed (as it is similar to `dqType`)
* `description` field is added
* `talend.component.semanticType` is removed
* `talend.component.dqType` is removed
* `talend.component.qualityAggregate` is removed

Some properties comming from TCK are also impacted:

* `talend.component.label` is renamed `originalFieldName`
* `talend.component.DATETIME` is renamed `isDatetime`


## JSON serializer/deserializer

In order to be able to serialize/deserialize the JSON payload with `"null"` avro format. We need to use a custom `ObjectMapper`.

This custom mapper is defined like that:

```java
public class DatasetSchemaMapperConfiguration {

    public static ObjectMapper datasetSchemaObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == DatasetFieldTypeSchema.DatasetFieldTypeSchemaBuilder.class){
                    return new DatasetFieldTypeDeserializer((BuilderBasedDeserializer)deserializer);
                }
                return deserializer;
            }
        });
        objectMapper.getSerializerProvider().setNullValueSerializer(new NullTypeStringSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

}
```

## Validate payload based on JSON Schema

There is a couple of existing libraries to validate a payload according to a JSON schema: https://json-schema.org/implementations.html#validators.

For our test we use:

```xml
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.0.64</version>
    <scope>test</scope>
</dependency>
```

## Dataset schema POJOs

Some lombok POJOs are available under `org.talend.daikon.schema.dataset`.

All POJOs define an attribute:

```java
@JsonAnySetter
@Singular
Map<String, Object> additionalProperties;
```

In order to serialize/deserialize additional properties not defined on the schema. These properties are unwrapped during the serialization process through:

```java
@JsonAnyGetter
@JsonUnwrapped
// workaround in order to be able to unwrapped @JsonAnySetter field
public Map getAdditionalProperties() {
    return additionalProperties;
}
```
