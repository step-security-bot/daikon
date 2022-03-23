package org.talend.tql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.talend.tql.model.TqlElement;

public class TestTqlParser_Comply extends TestTqlParser_Abstract {

    @Test
    public void testParseFieldCompliesPattern1() throws Exception {
        TqlElement tqlElement = doTest("name complies 'aaaaaaa'");
        String expected = "OrExpression{expressions=[AndExpression{expressions=[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='aaaaaaa'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern2() throws Exception {
        TqlElement tqlElement = doTest("name complies 'Aaaaaaa'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='Aaaaaaa'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern3() throws Exception {
        TqlElement tqlElement = doTest("name complies 'Aaaaaa 9aaa'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='Aaaaaa 9aaa'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern4() throws Exception {
        TqlElement tqlElement = doTest("name complies 'Aaa Aaaa'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='Aaa Aaaa'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern5() throws Exception {
        TqlElement tqlElement = doTest("name complies 'Aaaa_99'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='Aaaa_99'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern6() throws Exception {
        TqlElement tqlElement = doTest("name complies ']ss@'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern=']ss@'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern7() throws Exception {
        TqlElement tqlElement = doTest("name complies 'Aaaa أبجد Aaaa'");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern='Aaaa أبجد Aaaa'}]}]}";
        assertEquals(expected, tqlElement.toString());
    }

    @Test
    public void testParseFieldCompliesPattern8() throws Exception {
        TqlElement tqlElement = doTest("name complies ''");
        String expected = "OrExpression{expressions=[AndExpression{expressions="
                + "[FieldCompliesPattern{field='FieldReference{path='name'}', pattern=''}]}]}";
        assertEquals(expected, tqlElement.toString());
    }
}
