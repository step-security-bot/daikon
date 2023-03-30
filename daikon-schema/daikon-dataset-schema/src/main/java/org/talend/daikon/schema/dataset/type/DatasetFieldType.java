package org.talend.daikon.schema.dataset.type;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DatasetFieldType {
    BOOLEAN("boolean"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BYTES("bytes"),
    STRING("string"),
    RECORD("record"),
    ENUM("enum"),
    ARRAY("array"),
    MAP("map"),
    FIXED("fixed");

    private final String type;

    @JsonValue
    public String getType() {
        return type.toLowerCase();
    }
}
