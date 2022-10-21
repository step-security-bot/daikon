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

public class IsInvalidTest extends FunctionTest {

    private static IsInvalid isInvalid;

    @BeforeAll
    public static void setUp() {
        isInvalid = spy(IsInvalid.class);
    }

    /*
     * FYI : Because the isInvalid() custom function uses the IsOfTypeUtility, there's useless to have here
     * a complete bunch of tests
     */

    @Nested
    class IsCalledViaDselInterpreter {

        @DisplayName("Custom 'isInvalid' Function is called via the DSEL interpreter")
        @Test
        public void shouldWork() {
            testEvalExpression(true, "isInvalid('897', 'Boolean')");
            testEvalExpression(false, "isInvalid('897', 'iNTEGeR')");
        }

        @DisplayName("Custom 'isInvalid' Function is called with throwing Exception via the DSEL interpreter")
        @Test
        public void shouldThrowException() {
            testEvalExpressionThrowsFunctionException("isInvalid()");
            testEvalExpressionThrowsFunctionException("isInvalid('51')");
            testEvalExpressionThrowsFunctionException("isInvalid('28941', '', '')");
            testEvalExpressionThrowsFunctionException("isInvalid('897', integer)");
            testEvalExpressionThrowsFunctionException("isInvalid('897', 'integer', 'integer')");
        }

    }

    @Nested
    class SemanticType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "AIRPORT_CODE"));
        }

        @DisplayName("Integer conversion")
        @Test
        public void integerConversion() {
            assertTrue((Boolean) isInvalid.call(context, 12, "AIRPORT_CODE"));
        }
    }

    @Nested
    class StringType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "String"));
        }

        @DisplayName("Integer is of type String")
        @Test
        public void integerIsOfTypeString() {
            assertFalse((Boolean) isInvalid.call(context, 12, "String"));
        }

        @DisplayName("Double is of type String")
        @Test
        public void doubleIsOfTypeString() {
            assertFalse((Boolean) isInvalid.call(context, 12D, "String"));
        }

        @DisplayName("Float is of type String")
        @Test
        public void floatIsOfTypeString() {
            assertFalse((Boolean) isInvalid.call(context, 12f, "String"));
        }

        @DisplayName("Boolean is of type String")
        @Test
        public void booleanIsOfTypeString() {
            assertFalse((Boolean) isInvalid.call(context, true, "String"));
        }

        @DisplayName("Date is of type String")
        @Test
        public void dateIsOfTypeString() {
            assertFalse((Boolean) isInvalid.call(context, from(Instant.now()), "String"));
        }
    }

    @Nested
    class IntegerType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "Integer"));
        }

        @DisplayName("Integer is of type Integer")
        @Test
        public void integerIsOfTypeInteger() {
            assertFalse((Boolean) isInvalid.call(context, 12, "Integer"));
        }

        @DisplayName("Double is not of type Integer")
        @Test
        public void doubleIsNotOfTypeInteger() {
            assertTrue((Boolean) isInvalid.call(context, 12D, "Integer"));
        }

        @DisplayName("Float is not of type Integer")
        @Test
        public void floatIsNotOfTypeInteger() {
            assertTrue((Boolean) isInvalid.call(context, 12f, "Integer"));
        }

        @DisplayName("Boolean is not of type Integer")
        @Test
        public void booleanIsNotOfTypeInteger() {
            assertTrue((Boolean) isInvalid.call(context, true, "Integer"));
        }

        @DisplayName("Date is not of type Integer")
        @Test
        public void dateIsNotOfTypeInteger() {
            assertTrue((Boolean) isInvalid.call(context, from(Instant.now()), "Integer"));
        }
    }

    @Nested
    class DoubleType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "Double"));
        }

        @DisplayName("Integer is of type Double")
        @Test
        public void integerIsOfTypeDouble() {
            assertFalse((Boolean) isInvalid.call(context, 12, "Double"));
        }

        @DisplayName("Double is of type Double")
        @Test
        public void doubleIsOfTypeDouble() {
            assertFalse((Boolean) isInvalid.call(context, 12D, "Double"));
        }

        @DisplayName("Float is of type Double")
        @Test
        public void floatIsOfTypeDouble() {
            assertFalse((Boolean) isInvalid.call(context, 12f, "Double"));
        }

        @DisplayName("Boolean is not of type Double")
        @Test
        public void booleanIsNotOfTypeDouble() {
            assertTrue((Boolean) isInvalid.call(context, true, "Double"));
        }

        @DisplayName("Date is not of type Double")
        @Test
        public void dateIsNotOfTypeDouble() {
            assertTrue((Boolean) isInvalid.call(context, from(Instant.now()), "Double"));
        }
    }

    @Nested
    class BooleanType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "Boolean"));
        }

        @DisplayName("Integer is not of type Boolean")
        @Test
        public void integerIsNotOfTypeBoolean() {
            assertTrue((Boolean) isInvalid.call(context, 12, "Boolean"));
        }

        @DisplayName("Double is not of type Boolean")
        @Test
        public void doubleIsNotOfTypeBoolean() {
            assertTrue((Boolean) isInvalid.call(context, 12D, "Boolean"));
        }

        @DisplayName("Float is not of type Boolean")
        @Test
        public void floatIsNotOfTypeBoolean() {
            assertTrue((Boolean) isInvalid.call(context, 12f, "Boolean"));
        }

        @DisplayName("Boolean is of type Boolean")
        @Test
        public void booleanIsOfTypeBoolean() {
            assertFalse((Boolean) isInvalid.call(context, true, "Boolean"));
        }

        @DisplayName("Date is not of type Boolean")
        @Test
        public void dateIsNotOfTypeBoolean() {
            assertTrue((Boolean) isInvalid.call(context, from(Instant.now()), "Boolean"));
        }
    }

    @Nested
    class DateType {

        @DisplayName("empty is not invalid")
        @Test
        public void emptyIsNotInvalid() {
            assertFalse((Boolean) isInvalid.call(context, "", "Date"));
        }

        @DisplayName("Integer is not of type Date")
        @Test
        public void integerIsNotOfTypeDate() {
            assertTrue((Boolean) isInvalid.call(context, 12, "Date"));
        }

        @DisplayName("Double is not of type Date")
        @Test
        public void doubleIsNotOfTypeDate() {
            assertTrue((Boolean) isInvalid.call(context, 12D, "Date"));
        }

        @DisplayName("Float is not of type Date")
        @Test
        public void floatIsNotOfTypeDate() {
            assertTrue((Boolean) isInvalid.call(context, 12f, "Date"));
        }

        @DisplayName("Boolean is not of type Date")
        @Test
        public void booleanIsNotOfTypeDate() {
            assertTrue((Boolean) isInvalid.call(context, true, "Date"));
        }

        @DisplayName("Date is of type Date")
        @Test
        public void dateIsOfTypeDate() {
            assertFalse((Boolean) isInvalid.call(context, from(Instant.now()), "Date"));
        }
    }

}
