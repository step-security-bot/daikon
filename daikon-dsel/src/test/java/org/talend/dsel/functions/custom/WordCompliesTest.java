package org.talend.dsel.functions.custom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WordCompliesTest extends FunctionTest {

    private static WordComplies wordComplies;

    @BeforeAll
    public static void setUp() {
        wordComplies = spy(WordComplies.class);
    }

    @DisplayName("Value should match the word pattern")
    @Test
    public void valueShouldMatchPattern() {
        assertTrue((Boolean) wordComplies.call(context, "name", "[word]"));
    }

    @DisplayName("Value should not match the pattern")
    @Test
    public void valueShouldNotMatchPattern() {
        assertFalse((Boolean) wordComplies.call(context, "value", "[number]"));
    }

    @DisplayName("Custom 'wordComplies' Function is called via the DSEL interpreter")
    @Test
    public void isCalledViaDSELInterpreter() {
        testEvalExpression(true, "wordComplies('Here is 1 TEST', '[Word] [word] [digit] [WORD]')");
        testEvalExpression(false, "wordComplies('', '[digit]')");
        testEvalExpression(true, "wordComplies('Example123@domain.com', '[Word][number]@[word].[word]')");
        testEvalExpression(false, "wordComplies('name', '')");
        testEvalExpression(true, "wordComplies('', '')");
    }
}
