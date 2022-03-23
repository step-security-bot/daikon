package org.talend.tql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.tql.model.TqlElement;

public class TestTqlParser_Between extends TestTqlParser_Abstract {

    @Test
    public void testParseFieldBetweenQuoted() throws Exception {
        TqlElement tqlElement = doTest("field1 between ['value1', 'value2']");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=QUOTED_VALUE, value='value1'}, right=LiteralValue{literal=QUOTED_VALUE, value='value2'}, isLowerOpen=false, isUpperOpen=false}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenInt() throws Exception {
        TqlElement tqlElement = doTest("field1 between [123, 456]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=INT, value='123'}, right=LiteralValue{literal=INT, value='456'}, isLowerOpen=false, isUpperOpen=false}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenIntLowerOpen() throws Exception {
        TqlElement tqlElement = doTest("field1 between ]123, 456]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=INT, value='123'}, right=LiteralValue{literal=INT, value='456'}, isLowerOpen=true, isUpperOpen=false}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenIntUpperOpen() throws Exception {
        TqlElement tqlElement = doTest("field1 between [123, 456[");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=INT, value='123'}, right=LiteralValue{literal=INT, value='456'}, isLowerOpen=false, isUpperOpen=true}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenIntBothOpen() throws Exception {
        TqlElement tqlElement = doTest("field1 between ]123, 456[");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=INT, value='123'}, right=LiteralValue{literal=INT, value='456'}, isLowerOpen=true, isUpperOpen=true}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenDecimal() throws Exception {
        TqlElement tqlElement = doTest("field1 between [123.45, 456.78]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldBetweenExpression{field='FieldReference{path='field1'}', left=LiteralValue{literal=DECIMAL, value='123.45'}, right=LiteralValue{literal=DECIMAL, value='456.78'}, isLowerOpen=false, isUpperOpen=false}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenWrongValueString() {
        assertThrows(IllegalArgumentException.class, () -> {
            doTest("field1 between [a, b]");
        });
    }

    @Test
    public void testParseFieldBetweenWrongValueBoolean() {
        assertThrows(IllegalArgumentException.class, () -> {
            doTest("field1 between [true, false]");
        });
    }
}
