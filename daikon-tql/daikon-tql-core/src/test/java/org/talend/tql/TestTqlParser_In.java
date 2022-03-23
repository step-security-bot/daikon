package org.talend.tql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.TqlElement;

public class TestTqlParser_In extends TestTqlParser_Abstract {

    @Test
    public void testParseFieldBetweenQuoted1() throws Exception {
        TqlElement tqlElement = doTest("field1 in ['value1']");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=QUOTED_VALUE, value='value1'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenQuoted2() throws Exception {
        TqlElement tqlElement = doTest("field1 in ['value1', 'value2']");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=QUOTED_VALUE, value='value1'}, LiteralValue{literal=QUOTED_VALUE, value='value2'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenQuoted5() throws Exception {
        TqlElement tqlElement = doTest("field1 in ['value1', 'value2', 'value3', 'value4', 'value5']");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=QUOTED_VALUE, value='value1'}, LiteralValue{literal=QUOTED_VALUE, value='value2'}, LiteralValue{literal=QUOTED_VALUE, value='value3'}, LiteralValue{literal=QUOTED_VALUE, value='value4'}, LiteralValue{literal=QUOTED_VALUE, value='value5'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenInt1() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=INT, value='11'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenInt2() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11, 22]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=INT, value='11'}, LiteralValue{literal=INT, value='22'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenInt5() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11, 22, 33, 44, 55]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=INT, value='11'}, LiteralValue{literal=INT, value='22'}, LiteralValue{literal=INT, value='33'}, LiteralValue{literal=INT, value='44'}, LiteralValue{literal=INT, value='55'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenDecimal1() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11.11]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=DECIMAL, value='11.11'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenDecimal2() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11.11, 22.22]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=DECIMAL, value='11.11'}, LiteralValue{literal=DECIMAL, value='22.22'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenDecimal5() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11.11, 22.22, 33.33, 44.44, 55.55]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=DECIMAL, value='11.11'}, LiteralValue{literal=DECIMAL, value='22.22'}, LiteralValue{literal=DECIMAL, value='33.33'}, LiteralValue{literal=DECIMAL, value='44.44'}, LiteralValue{literal=DECIMAL, value='55.55'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenBoolean1() throws Exception {
        TqlElement tqlElement = doTest("field1 in [true]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=BOOLEAN, value='true'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenBoolean2() throws Exception {
        TqlElement tqlElement = doTest("field1 in [true, false]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=BOOLEAN, value='true'}, LiteralValue{literal=BOOLEAN, value='false'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenMix() throws Exception {
        TqlElement tqlElement = doTest("field1 in [11, 22.22, true]");
        String expected = "OrExpression{expressions=[AndExpression{expressions=["
                + "FieldInExpression{field='FieldReference{path='field1'}', values=[LiteralValue{literal=INT, value='11'}, LiteralValue{literal=DECIMAL, value='22.22'}, LiteralValue{literal=BOOLEAN, value='true'}]}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldBetweenWrongValueString() throws Exception {
        assertThrows(TqlException.class, () -> {
            doTest("field1 in [a, b]");
        });
    }
}
