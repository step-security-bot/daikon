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
package org.talend.daikon.avro.visitor.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

public class TestTraversalPath {

    @Test
    public void testRoot() {
        Schema schema = createSimpleSchema();
        TraversalPath path = TraversalPath.create(schema);
        TraversalPath.TraversalPathElement root = path.last();
        assertTrue(root instanceof org.talend.daikon.avro.visitor.path.TraversalPath.RootPathElement);
        assertEquals("/", path.toString());
        assertEquals(schema, root.getSchema());
    }

    @Test
    public void testAppend() {
        Schema schema = createSimpleSchema();
        TraversalPath path = TraversalPath.create(schema).append("step1").append("step2");
        Iterator<TraversalPath.TraversalPathElement> elements = path.iterator();
        TraversalPath.TraversalPathElement root = elements.next();
        assertTrue(root instanceof TraversalPath.RootPathElement);
        assertEquals(schema, root.getSchema());

        TraversalPath.TraversalPathElement step1 = elements.next();
        assertEquals("step1", step1.getName());
        assertEquals(0, step1.getPosition());
        assertEquals(schema.getField("step1").schema(), step1.getSchema());

        TraversalPath.TraversalPathElement step2 = elements.next();
        assertEquals("step2", step2.getName());
        assertEquals(0, step2.getPosition());
        assertEquals(schema.getField("step1").schema().getField("step2").schema(), step2.getSchema());

        assertEquals("/step1/step2", path.toString());

        assertEquals(root, path.root());
        assertEquals(step2, path.last());
    }

    @Test
    public void testAppendByIndex() {
        Schema schema = createSimpleSchema();
        TraversalPath path = TraversalPath.create(schema).append(0).append(0);
        assertEquals("/step1/step2", path.toString());
    }

    @Test
    public void testAppendByBadIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            Schema schema = createSimpleSchema();
            TraversalPath.create(schema).append(35);
        });
    }

    @Test
    public void testAppendNameNotFound() {
        assertThrows(NullPointerException.class, () -> {
            Schema schema = createSimpleSchema();
            TraversalPath.create(schema).append("step1").append("unknown");
        });

    }

    @Test
    public void testAppendNotARecord() {
        assertThrows(AvroRuntimeException.class, () -> {
            Schema schema = createSimpleSchema();
            TraversalPath.create(schema).append("step1").append("step2").append("step3");
        });
    }

    @Test
    public void testAppendArrayIndex() {
        Schema schema = createSimpleSchema();

        TraversalPath path = TraversalPath.create(schema).append("array").appendArrayIndex(5);

        Iterator<TraversalPath.TraversalPathElement> elements = path.iterator();

        TraversalPath.TraversalPathElement root = elements.next();
        assertTrue(root instanceof TraversalPath.RootPathElement);
        assertEquals(schema, root.getSchema());

        TraversalPath.TraversalPathElement array = elements.next();
        assertEquals("array", array.getName());
        assertEquals(1, array.getPosition());
        assertEquals(schema.getField("array").schema(), array.getSchema());

        TraversalPath.ArrayItemPathElement arrayElement = (TraversalPath.ArrayItemPathElement) elements.next();
        assertEquals("array", arrayElement.getName());
        assertEquals(1, arrayElement.getPosition());
        assertEquals(5, arrayElement.getIndex());
        assertEquals(schema.getField("array").schema().getElementType(), arrayElement.getSchema());

        assertEquals("/array[5]", path.toString());
    }

    @Test
    public void testAppendMapEntry() {
        Schema schema = createSimpleSchema();

        TraversalPath path = TraversalPath.create(schema).append("map").appendMapEntry("key1");

        Iterator<TraversalPath.TraversalPathElement> elements = path.iterator();

        TraversalPath.TraversalPathElement root = elements.next();
        assertTrue(root instanceof TraversalPath.RootPathElement);
        assertEquals(schema, root.getSchema());

        TraversalPath.TraversalPathElement map = elements.next();
        assertEquals("map", map.getName());
        assertEquals(2, map.getPosition());
        assertEquals(schema.getField("map").schema(), map.getSchema());

        TraversalPath.MapEntryPathElement entry = (TraversalPath.MapEntryPathElement) elements.next();
        assertEquals("map", entry.getName());
        assertEquals(2, entry.getPosition());
        assertEquals("key1", entry.getKey());
        assertEquals(schema.getField("map").schema().getValueType(), entry.getSchema());
    }

    @Test
    public void testJSONPathPrinter() throws Exception {
        Schema schema = createSimpleSchema();

        assertEquals("$.step1.step2",
                TraversalPath.create(schema).append("step1").append("step2").toString(new JsonPathPrinter()));

        assertEquals("$.array[5]",
                TraversalPath.create(schema).append("array").appendArrayIndex(5).toString(new JsonPathPrinter()));

        assertEquals("$.map.key1",
                TraversalPath.create(schema).append("map").appendMapEntry("key1").toString(new JsonPathPrinter()));
    }

    @Test
    public void testJSONPathPrinterBrackets() throws Exception {
        Schema schema = createSimpleSchema();
        assertEquals("$['step1']['step2']", TraversalPath.create(schema).append("step1").append("step2")
                .toString(new JsonPathPrinter(JsonPathPrinter.JsonPathStyle.BRACKETS)));

        assertEquals("$['array'][5]", TraversalPath.create(schema).append("array").appendArrayIndex(5)
                .toString(new JsonPathPrinter(JsonPathPrinter.JsonPathStyle.BRACKETS)));

        assertEquals("$['map']['key1']", TraversalPath.create(schema).append("map").appendMapEntry("key1")
                .toString(new JsonPathPrinter(JsonPathPrinter.JsonPathStyle.BRACKETS)));
    }

    @Test
    public void testIdentity() throws Exception {
        Schema schema1 = createSimpleSchema();
        TraversalPath path1 = TraversalPath.create(schema1).append("step1").append("step2");

        Schema schema2 = createSimpleSchema();
        TraversalPath path2 = TraversalPath.create(schema2).append("step1").append("step2");

        assertEquals(path1, path2);
    }

    private Schema createSimpleSchema() {
        return SchemaBuilder.record("record").fields().name("step1").type().record("step1Type").fields().name("step2").type()
                .stringType().noDefault().endRecord().noDefault().name("array").type().array().items().intType().noDefault()
                .name("map").type().map().values().intType().noDefault().endRecord();
    }
}
