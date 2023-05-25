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

Some properties coming from TCK are also impacted:

* `talend.component.label` is renamed `originalFieldName`
* `talend.component.DATETIME` is renamed `isDatetime`


## Serialization/deserialization

Any specificities of the serialization/deserialization should be handled via Jackson annotations wherever possible. Let's try to avoid using a custom object mapper because users of this module might not be able to use a specific mapper in their context, or it might be hard to configure. Sometimes, the web framework handles things behind the scenes for example.

There are currently some specific use-cases that are handled by classes in the [mapper package](./src/main/java/org/talend/daikon/schema/dataset/mapper) and documented in the Javadocs.

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
