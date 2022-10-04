package org.talend.dsel.functions.custom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IsValidTest extends FunctionTest {

    private static IsValid isValid;

    @BeforeAll
    public static void setUp() {
        isValid = spy(IsValid.class);
    }

    // FYI : Because the isInvalid() custom function uses the isValid() custom function, there's useless to have here
    // a complete bunch of tests. That's why here there's only 2 tests to check the expected behavior of isValid().

    @DisplayName("String int is valid as an Integer")
    @Test
    public void intStringIsInteger() {
        assertTrue((Boolean) isValid.call(context, "12", "Integer"));
    }

    @DisplayName("String double is not valid as an Integer")
    @Test
    public void intDoubleNotInteger() {
        assertFalse((Boolean) isValid.call(context, "12.0", "Integer"));
    }

    @DisplayName("Custom 'isValid' Function is called via the DSEL interpreter")
    @Test
    public void isCalledViaDSELInterpreter() {
        testEvalExpression(true, "isValid('671.0819', 'DEciMAl')");
        testEvalExpression(false, "isValid('False', 'DATE')");
    }

    @DisplayName("Custom 'isValid' Function is called with throwing Exception via the DSEL interpreter")
    @Test
    public void isCalledWithThrowingExceptionViaDSELInterpreter() {
        testEvalExpressionThrowsFunctionException("isValid()");
        testEvalExpressionThrowsFunctionException("isValid('113218')");
        testEvalExpressionThrowsFunctionException("isValid('54', '', '')");
    }
}
