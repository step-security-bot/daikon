package org.talend.dsel.functions.custom;

import static java.util.Date.from;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dsel.exception.DQCategoryNotFoundException;

public class IsOfTypeTest extends FunctionTest {

    @Nested
    class SemanticType {

        @DisplayName("Airport code")
        @Test
        public void testAirport() {
            assertTrue(IsOfTypeUtility.evaluate(context, "AAA", "AIRPORT_CODE"));
            assertFalse(IsOfTypeUtility.evaluate(context, "BLABLA", "AIRPORT_CODE"));
        }

        @DisplayName("Unknown category")
        @Test
        public void testUnknownCategory() {
            assertThrows(DQCategoryNotFoundException.class, () -> IsOfTypeUtility.evaluate(context, "AAA", "UNKNOWN_CATEGORY"));
            assertThrows(DQCategoryNotFoundException.class,
                    () -> assertFalse(IsOfTypeUtility.evaluate(context, "BLABLA", "UNKNOWN_CATEGORY")));
        }

        @DisplayName("Integer category is not prevalent on Integer DataTypeEnum")
        @Test
        public void integerCategoryIgnored() {
            String value = "t";
            setUpContextWithIntegerCategory();
            DQCategory integerCat = dictionarySnapshot.getDQCategoryByName("INTEGER");
            assertTrue(new SemanticQualityAnalyzer(dictionarySnapshot).isValid(integerCat, value));
            assertFalse(IsOfTypeUtility.evaluate(context, value, "INTEGER"));
        }
    }

    @Nested
    class StringType {

        @DisplayName("String is of type String")
        @Test
        public void stringIsOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, "just a String value", "String"));
        }

        @DisplayName("Integer as String is of type String")
        @Test
        public void integerAsStringIsOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, "12", "String"));
        }

        @DisplayName("Double as String is of type String")
        @Test
        public void doubleAsStringIsOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, "12D", "String"));
        }

        @DisplayName("Float as String is of type String")
        @Test
        public void floatAsStringIsOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, "12f", "String"));
        }

        @DisplayName("Boolean as String is of type String")
        @Test
        public void booleanAsStringIsNotOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, "true", "String"));
        }

        @DisplayName("Date as String is of type String")
        @Test
        public void dateAsStringIsOfTypeString() {
            assertTrue(IsOfTypeUtility.evaluate(context, from(Instant.now()).toString(), "String"));
        }
    }

    @Nested
    class IntegerType {

        @DisplayName("String is not of type Integer")
        @Test
        public void stringIsNotOfTypeInteger() {
            assertFalse(IsOfTypeUtility.evaluate(context, "just a String value", "Integer"));
        }

        @DisplayName("Integer as String is of type Integer")
        @Test
        public void integerAsStringIsOfTypeInteger() {
            assertTrue(IsOfTypeUtility.evaluate(context, "12", "Integer"));
        }

        @DisplayName("Double as String is not of type Integer")
        @Test
        public void doubleAsStringIsNotOfTypeInteger() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12D", "Integer"));
        }

        @DisplayName("Float as String is not of type Integer")
        @Test
        public void floatAsStringIsNotOfTypeInteger() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12f", "Integer"));
        }

        @DisplayName("Boolean as String is not of type Integer")
        @Test
        public void booleanAsStringIsNotOfTypeInteger() {
            assertFalse(IsOfTypeUtility.evaluate(context, "true", "Integer"));
        }

        @DisplayName("Date as String is not of type Integer")
        @Test
        public void dateAsStringIsNotOfTypeInteger() {
            assertFalse(IsOfTypeUtility.evaluate(context, from(Instant.now()).toString(), "Integer"));
        }
    }

    @Nested
    class DoubleType {

        @DisplayName("String is not of type Double")
        @Test
        public void stringIsNotOfTypeDouble() {
            assertFalse(IsOfTypeUtility.evaluate(context, "just a String value", "Double"));
        }

        @DisplayName("Integer as String is of type Double")
        @Test
        public void integerAsStringIsOfTypeDouble() {
            assertTrue(IsOfTypeUtility.evaluate(context, "12", "Double"));
        }

        @DisplayName("Double as String is not of type Double")
        @Test
        public void doubleAsStringIsNotOfTypeDouble() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12D", "Double"));
        }

        @DisplayName("Float as String is not of type Double")
        @Test
        public void floatAsStringIsNotOfTypeDouble() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12f", "Double"));
        }

        @DisplayName("Boolean as String is not of type Double")
        @Test
        public void booleanAsStringIsNotOfTypeDouble() {
            assertFalse(IsOfTypeUtility.evaluate(context, "true", "Double"));
        }

        @DisplayName("Date as String is not of type Double")
        @Test
        public void dateAsStringIsNotOfTypeDouble() {
            assertFalse(IsOfTypeUtility.evaluate(context, from(Instant.now()).toString(), "Double"));
        }
    }

    @Nested
    class BooleanType {

        @DisplayName("String is not of type Boolean")
        @Test
        public void stringIsNotOfTypeBoolean() {
            assertFalse(IsOfTypeUtility.evaluate(context, "just a String value", "Boolean"));
        }

        @DisplayName("Integer as String is not of type Boolean")
        @Test
        public void integerAsStringIsNotOfTypeBoolean() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12", "Boolean"));
        }

        @DisplayName("Double as String is nnot of type Boolean")
        @Test
        public void doubleAsStringIsNotOfTypeBoolean() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12D", "Boolean"));
        }

        @DisplayName("Float as String is not of type Boolean")
        @Test
        public void floatAsStringIsNotOfTypeBoolean() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12f", "Boolean"));
        }

        @DisplayName("Boolean as String is of type Boolean")
        @Test
        public void booleanAsStringIsOfTypeBoolean() {
            assertTrue(IsOfTypeUtility.evaluate(context, "true", "Boolean"));
        }

        @DisplayName("Date as String is not of type Boolean")
        @Test
        public void dateAsStringIsNotOfTypeBoolean() {
            assertFalse(IsOfTypeUtility.evaluate(context, from(Instant.now()).toString(), "Boolean"));
        }
    }

    @Nested
    class DateType {

        @DisplayName("String is not of type Date")
        @Test
        public void stringIsNotOfTypeDate() {
            assertFalse(IsOfTypeUtility.evaluate(context, "just a String value", "Date"));
        }

        @DisplayName("Integer as String is not of type Date")
        @Test
        public void integerAsStringIsNotOfTypeDate() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12", "Date"));
        }

        @DisplayName("Double as String is not of type Date")
        @Test
        public void doubleAsStringIsNotOfTypeDate() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12D", "Date"));
        }

        @DisplayName("Float as String is not of type Date")
        @Test
        public void floatAsStringIsNotOfTypeDate() {
            assertFalse(IsOfTypeUtility.evaluate(context, "12f", "Date"));
        }

        @DisplayName("Boolean as String is not of type Date")
        @Test
        public void booleanAsStringIsNotOfTypeDate() {
            assertFalse(IsOfTypeUtility.evaluate(context, "true", "Date"));
        }

        @DisplayName("Date as String is of type Date")
        @Test
        public void dateAsStringIsOfTypeDate() {
            assertTrue(IsOfTypeUtility.evaluate(context, from(Instant.now()).toString(), "Date"));
        }
    }

}
