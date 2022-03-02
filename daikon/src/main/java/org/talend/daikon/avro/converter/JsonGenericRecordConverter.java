// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.avro.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.talend.daikon.exception.TalendRuntimeException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Converts Json String to Avro Generic Record and vice-versa.
 * <p>
 * <b>ATTENTION:</b> This class doesn't handle the MAP Avro type.
 */
public class JsonGenericRecordConverter implements AvroConverter<String, GenericRecord> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Schema schema;

    /**
     * Constructor
     */
    public JsonGenericRecordConverter() {
    }

    /**
     * Constructor
     * 
     * @param schema the reference Avro schema used when converting to Avro record
     */
    public JsonGenericRecordConverter(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Class<String> getDatumClass() {
        return String.class;
    }

    @Override
    public String convertToDatum(GenericRecord record) {
        return record.toString();
    }

    /**
     * Converts Json String to Avro Generic Record.
     *
     * TalendRuntimeException thrown when an IOException or RuntimeException occurred.
     *
     * @param avroRecordAsJsonString string to convert
     * @return Avro Generic Record.
     */
    @Override
    public GenericRecord convertToAvro(String avroRecordAsJsonString) {
        try {
            Objects.requireNonNull(schema, "Schema should be provided when converting to Avro");
            JsonNode jsonNode = OBJECT_MAPPER.readTree(avroRecordAsJsonString);
            return convertJsonToAvroRecord(jsonNode, schema);
        } catch (IOException | TalendRuntimeException e) {
            throw TalendRuntimeException.createUnexpectedException(e.getCause());
        }
    }

    /**
     * Generates Avro Generic Record from a Json Node and a Schema.
     *
     * @param jsonNode to convert to Avro Generic Record
     * @param schema of jsonNode
     * @return Avro Generic Record
     */
    private GenericRecord convertJsonToAvroRecord(final JsonNode jsonNode, final Schema schema) {
        final GenericRecordBuilder outputRecord = new GenericRecordBuilder(schema);
        final Iterator<Map.Entry<String, JsonNode>> elements = jsonNode.fields();

        while (elements.hasNext()) {
            final Map.Entry<String, JsonNode> mapEntry = elements.next();
            final JsonNode nextNode = mapEntry.getValue();

            if (!(nextNode instanceof NullNode)) {
                if (nextNode instanceof ValueNode) {
                    outputRecord.set(mapEntry.getKey(), getValue(nextNode));
                } else if (nextNode instanceof ObjectNode) {
                    Schema fieldSchema = schema.getField(mapEntry.getKey()).schema();
                    GenericRecord record = convertJsonToAvroRecord(nextNode, fieldSchema);
                    outputRecord.set(mapEntry.getKey(), record);
                } else if (nextNode instanceof ArrayNode) {
                    List<Object> listRecords = new ArrayList<>();
                    Iterator<JsonNode> elementsIterator = nextNode.elements();
                    while (elementsIterator.hasNext()) {
                        JsonNode nodeTo = elementsIterator.next();
                        if (nodeTo instanceof ValueNode) {
                            listRecords.add(getValue(nodeTo));
                        } else {
                            Schema elementSchema = schema.getField(mapEntry.getKey()).schema().getElementType();
                            listRecords.add(convertJsonToAvroRecord(nodeTo, elementSchema));
                        }
                    }
                    outputRecord.set(mapEntry.getKey(), listRecords);
                }
            } else {
                outputRecord.set(mapEntry.getKey(), null);
            }
        }
        return outputRecord.build();
    }

    /**
     * Gets the inner value of a Json Node.
     */
    private Object getValue(JsonNode node) {
        if (node instanceof TextNode) {
            return node.textValue();
        } else if (node instanceof IntNode) {
            return node.intValue();
        } else if (node instanceof LongNode) {
            return node.longValue();
        } else if (node instanceof DoubleNode) {
            return node.doubleValue();
        } else if (node instanceof BooleanNode) {
            return node.booleanValue();
        }
        return null;
    }
}
