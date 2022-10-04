package org.talend.dsel.functions.custom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IsInvalidTest extends FunctionTest {

    private static IsInvalid isInvalid;

    @BeforeAll
    public static void setUp() {
        isInvalid = spy(IsInvalid.class);
    }

    // FYI : Because the isInvalid() custom function uses the isValid() custom function which in turn uses the
    // isOfType() custom function, there's useless to have here a complete bunch of tests.
    // That's why here there's only 2 tests to check the expected behavior of isInvalid().

    @DisplayName("String int is not invalid as an Integer")
    @Test
    public void intStringIsInteger() {
        assertFalse((Boolean) isInvalid.call(context, "12", "Integer"));
    }

    @DisplayName("String double is invalid as an Integer")
    @Test
    public void intDoubleNotInteger() {
        assertTrue((Boolean) isInvalid.call(context, "12.0", "Integer"));
    }

    @DisplayName("Custom 'isInvalid' Function is called via the DSEL interpreter")
    @Test
    public void isCalledViaDSELInterpreter() {
        testEvalExpression(true, "isInvalid('897', 'Boolean')");
        testEvalExpression(false, "isInvalid('897', 'iNTEGeR')");
    }

    @DisplayName("Custom 'isInvalid' Function is called with throwing Exception via the DSEL interpreter")
    @Test
    public void isCalledWithThrowingExceptionViaDSELInterpreter() {
        testEvalExpressionThrowsFunctionException("isInvalid()");
        testEvalExpressionThrowsFunctionException("isInvalid('51')");
        testEvalExpressionThrowsFunctionException("isInvalid('28941', '', '')");
    }
}
