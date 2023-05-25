# Common dataset sample description

The goal of this module is to provide a *common* dataset sample metadata description. 

This work is based on [Common-format-for-record](https://github.com/Talend/dataset-service/blob/develop/docs/engineering/TPGOV-1674/README.md#common-format-for-records).

## JSON Schema

JSON schemas representing a dataset sample metadata are located under `src/main/resources` folder. They define mandatory and none-mandatory fields and set a description for each field.


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

