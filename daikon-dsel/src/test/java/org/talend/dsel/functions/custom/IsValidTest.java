package org.talend.dsel.functions.custom;

import static java.util.Date.from;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class IsValidTest extends FunctionTest {

    private static IsValid isValid;

    @BeforeAll
    public static void setUp() {
        isValid = spy(IsValid.class);
    }

    /*
     * FYI : Because the isValid() custom function uses the IsOfTypeUtility, there's useless to have here
     * a complete bunch of tests
     */

    @Nested
    class IsCalledViaDselInterpreter {

        @DisplayName("Custom 'isValid' Function is called via the DSEL interpreter")
        @Test
        public void shouldWork() {
            testEvalExpression(true, "isValid(12f, 'dOuble')");
            testEvalExpression(false, "isValid('False', 'DATE')");
        }

        @DisplayName("Custom 'isValid' Function is called with throwing Exception via the DSEL interpreter")
        @Test
        public void shouldThrowException() {
            testEvalExpressionThrowsFunctionException("isValid()");
            testEvalExpressionThrowsFunctionException("isValid('113218')");
            testEvalExpressionThrowsFunctionException("isValid('54', '', '')");
            testEvalExpressionThrowsFunctionException("isValid('False', DATE)");
            testEvalExpressionThrowsFunctionException("isValid('False', 'date', 'date')");
        }
    }

    @Nested
    class SemanticType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "AIRPORT_CODE"));
        }

        @DisplayName("Integer conversion")
        @Test
        public void integerConversion() {
            assertFalse((Boolean) isValid.call(context, 12, "AIRPORT_CODE"));
        }
    }

    @Nested
    class StringType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "String"));
        }

        @DisplayName("Integer is of type String")
        @Test
        public void integerIsOfTypeString() {
            assertTrue((Boolean) isValid.call(context, 12, "String"));
        }

        @DisplayName("Float is of type String")
        @Test
        public void floatIsOfTypeString() {
            assertTrue((Boolean) isValid.call(context, 12f, "String"));
        }

        @DisplayName("Boolean is of type String")
        @Test
        public void booleanIsOfTypeString() {
            assertTrue((Boolean) isValid.call(context, true, "String"));
        }

        @DisplayName("Date is of type String")
        @Test
        public void dateIsOfTypeString() {
            assertTrue((Boolean) isValid.call(context, from(Instant.now()), "String"));
        }

    }

    @Nested
    class IntegerType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "Integer"));
        }

        @DisplayName("Integer is of type Integer")
        @Test
        public void integerIsOfTypeInteger() {
            assertTrue((Boolean) isValid.call(context, 12, "Integer"));
        }

        @DisplayName("Double is not of type Integer")
        @Test
        public void doubleIsNotOfTypeInteger() {
            assertFalse((Boolean) isValid.call(context, 12D, "Integer"));
        }

        @DisplayName("Float is not of type Integer")
        @Test
        public void floatIsNotOfTypeInteger() {
            assertFalse((Boolean) isValid.call(context, 12f, "Integer"));
        }

        @DisplayName("Boolean is not of type Integer")
        @Test
        public void booleanIsNotOfTypeInteger() {
            assertFalse((Boolean) isValid.call(context, true, "Integer"));
        }

        @DisplayName("Date is not of type Integer")
        @Test
        public void dateIsNotOfTypeInteger() {
            assertFalse((Boolean) isValid.call(context, from(Instant.now()), "Integer"));
        }
    }

    @Nested
    class DoubleType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "Double"));
        }

        @DisplayName("Integer is of type Double")
        @Test
        public void integerIsOfTypeDouble() {
            assertTrue((Boolean) isValid.call(context, 12, "Double"));
        }

        @DisplayName("Double is of type Double")
        @Test
        public void doubleIsOfTypeDouble() {
            assertTrue((Boolean) isValid.call(context, 12D, "Double"));
        }

        @DisplayName("Float is of type Double")
        @Test
        public void floatIsOfTypeDouble() {
            assertTrue((Boolean) isValid.call(context, 12f, "Double"));
        }

        @DisplayName("Boolean is not of type Double")
        @Test
        public void booleanIsNotOfTypeDouble() {
            assertFalse((Boolean) isValid.call(context, true, "Double"));
        }

        @DisplayName("Date is not of type Double")
        @Test
        public void dateIsNotOfTypeDouble() {
            assertFalse((Boolean) isValid.call(context, from(Instant.now()), "Double"));
        }
    }

    @Nested
    class BooleanType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "Boolean"));
        }

        @DisplayName("Integer is not of type Boolean")
        @Test
        public void integerIsNotOfTypeBoolean() {
            assertFalse((Boolean) isValid.call(context, 12, "Boolean"));
        }

        @DisplayName("Double is not of type Boolean")
        @Test
        public void doubleIsNotOfTypeBoolean() {
            assertFalse((Boolean) isValid.call(context, 12D, "Boolean"));
        }

        @DisplayName("Float is not of type Boolean")
        @Test
        public void floatIsNotOfTypeBoolean() {
            assertFalse((Boolean) isValid.call(context, 12f, "Boolean"));
        }

        @DisplayName("Boolean is of type Boolean")
        @Test
        public void booleanIsOfTypeBoolean() {
            assertTrue((Boolean) isValid.call(context, true, "Boolean"));
        }

        @DisplayName("Date is not of type Boolean")
        @Test
        public void dateIsNotOfTypeBoolean() {
            assertFalse((Boolean) isValid.call(context, from(Instant.now()), "Boolean"));
        }
    }

    @Nested
    class DateType {

        @DisplayName("empty is not valid")
        @Test
        public void emptyIsNotValid() {
            assertFalse((Boolean) isValid.call(context, "", "Date"));
        }

        @DisplayName("Integer is not of type Date")
        @Test
        public void integerIsNotOfTypeDate() {
            assertFalse((Boolean) isValid.call(context, 12, "Date"));
        }

        @DisplayName("Double is not of type Date")
        @Test
        public void doubleIsNotOfTypeDate() {
            assertFalse((Boolean) isValid.call(context, 12D, "Date"));
        }

        @DisplayName("Float is not of type Date")
        @Test
        public void floatIsNotOfTypeDate() {
            assertFalse((Boolean) isValid.call(context, 12f, "Date"));
        }

        @DisplayName("Boolean is not of type Date")
        @Test
        public void booleanIsNotOfTypeDate() {
            assertFalse((Boolean) isValid.call(context, true, "Date"));
        }

        @DisplayName("Date is of type Date")
        @Test
        public void dateIsOfTypeDate() {
            assertTrue((Boolean) isValid.call(context, from(Instant.now()), "Date"));
        }
    }
}
