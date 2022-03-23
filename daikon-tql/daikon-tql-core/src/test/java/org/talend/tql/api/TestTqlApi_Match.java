package org.talend.tql.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.talend.tql.api.TqlBuilder.match;

import org.junit.jupiter.api.Test;
import org.talend.tql.TestTqlParser_Abstract;
import org.talend.tql.model.TqlElement;

public class TestTqlApi_Match extends TestTqlParser_Abstract {

    @Test
    public void testApiFieldMatchPattern1() throws Exception {
        // TQL native query
        TqlElement expected = doTest("name ~ '^[A-Z][a-z]*$'");
        // TQL api query
        TqlElement tqlElement = match("name", "^[A-Z][a-z]*$");
        assertEquals(expected.toString(), tqlElement.toString());
    }

    @Test
    public void testApiFieldMatchPattern2() throws Exception {
        // TQL native query
        TqlElement expected = doTest("name ~ '\\d'");
        // TQL api query
        TqlElement tqlElement = match("name", "\\d");
        assertEquals(expected.toString(), tqlElement.toString());
    }

    @Test
    public void testApiFieldMatchPattern3() throws Exception {
        // TQL native query
        TqlElement expected = doTest("name ~ ''");
        // TQL api query
        TqlElement tqlElement = match("name", "");
        assertEquals(expected.toString(), tqlElement.toString());
    }

    @Test
    public void testApiFieldMatchPattern4() throws Exception {
        // TQL native query
        TqlElement expected = doTest("name ~ '^[A-Z][\\'][a-z]*$'");
        // TQL api query
        TqlElement tqlElement = match("name", "^[A-Z]['][a-z]*$");
        assertEquals(expected.toString(), tqlElement.toString());
    }

}
