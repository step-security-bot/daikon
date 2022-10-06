package org.talend.dsel.functions.custom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CompliesTest extends FunctionTest {

    private static Complies complies;

    @BeforeAll
    public static void setUp() {
        complies = spy(Complies.class);
    }

    @DisplayName("Value should match the pattern")
    @Test
    public void valueShouldMatchPattern() {
        assertTrue((Boolean) complies.call(context, "vaLue", "aaAaa"));
    }

    @DisplayName("Value should not match the pattern")
    @Test
    public void valueShouldNotMatchPattern() {
        assertFalse((Boolean) complies.call(context, "value", "99999"));
    }

    @DisplayName("Custom 'complies' Function is called via the DSEL interpreter")
    @Test
    public void isCalledViaDSELInterpreter() {
        testEvalExpression(true, "complies('vAlUE', 'aAaAA')");
        testEvalExpression(false, "complies('value', '99999')");
        testEvalExpression(true, "complies('28q4wG5145p', '99a9aA9999a')");
        testEvalExpression(false, "complies('value', '')");
        testEvalExpression(true, "complies('', '')");
    }
}
