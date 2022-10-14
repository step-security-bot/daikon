package org.talend.dsel.functions.custom;

import static java.util.Date.from;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.CustomDictionaryHolder;
import org.talend.dataquality.semantic.api.DeletableDictionarySnapshotOpener;
import org.talend.dataquality.semantic.api.SemanticProperties;
import org.talend.dataquality.semantic.model.CategoryType;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.model.DQRegEx;
import org.talend.dataquality.semantic.model.DQValidator;
import org.talend.dataquality.semantic.model.MainCategory;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.dsel.exception.DQCategoryNotFoundException;
import org.talend.maplang.el.interpreter.api.DselHPathStore;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.hpath.HPathStore;

public class IsOfTypeTest extends FunctionTest {

    private static IsOfType isOfType;

    @BeforeAll
    public static void setUp() {
        isOfType = spy(IsOfType.class);
    }

    @Nested
    class SemanticType {

        @DisplayName("Airport code")
        @Test
        public void testAirport() {
            assertTrue((Boolean) isOfType.call(context, "AAA", "AIRPORT_CODE"));
            assertFalse((Boolean) isOfType.call(context, "BLABLA", "AIRPORT_CODE"));
        }

        @DisplayName("Unknown category")
        @Test
        public void testUnknownCategory() {
            assertThrows(DQCategoryNotFoundException.class, () -> isOfType.call(context, "AAA", "UNKNOWN_CATEGORY"));
            assertThrows(DQCategoryNotFoundException.class,
                    () -> assertFalse((Boolean) isOfType.call(context, "BLABLA", "UNKNOWN_CATEGORY")));
        }

        @DisplayName("Integer category is not prevalent on Integer DataTypeEnum")
        @Test
        public void integerCategoryIgnored() {
            String value = "t";
            setUpContextWithIntegerCategory();
            DQCategory integerCat = dictionarySnapshot.getDQCategoryByName("INTEGER");
            assertTrue(new SemanticQualityAnalyzer(dictionarySnapshot).isValid(integerCat, value));
            assertFalse((Boolean) isOfType.call(context, value, "INTEGER"));
        }

        @DisplayName("Integer conversion")
        @Test
        public void integerConversion() {
            assertFalse((Boolean) isOfType.call(context, 12, "AIRPORT_CODE"));
        }
    }

    @Nested
    class StringType {

        @DisplayName("String is of type String")
        @Test
        public void stringIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, "just a String value", "String"));
        }

        @DisplayName("Integer is of type String")
        @Test
        public void integerIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, 12, "String"));
        }

        @DisplayName("Integer as String is of type String")
        @Test
        public void integerAsStringIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, "12", "String"));
        }

        @DisplayName("Double is of type String")
        @Test
        public void doubleIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, 12D, "String"));
        }

        @DisplayName("Double as String is of type String")
        @Test
        public void doubleAsStringIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, "12D", "String"));
        }

        @DisplayName("Float is of type String")
        @Test
        public void floatIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, 12f, "String"));
        }

        @DisplayName("Float as String is of type String")
        @Test
        public void floatAsStringIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, "12f", "String"));
        }

        @DisplayName("Boolean is of type String")
        @Test
        public void booleanIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, true, "String"));
        }

        @DisplayName("Boolean as String is of type String")
        @Test
        public void booleanAsStringIsNotOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, "true", "String"));
        }

        @DisplayName("Date is of type String")
        @Test
        public void dateIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, from(Instant.now()), "String"));
        }

        @DisplayName("Date as String is of type String")
        @Test
        public void dateAsStringIsOfTypeString() {
            assertTrue((Boolean) isOfType.call(context, from(Instant.now()).toString(), "String"));
        }
    }

    @Nested
    class IntegerType {

        @DisplayName("String is not of type Integer")
        @Test
        public void stringIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, "just a String value", "Integer"));
        }

        @DisplayName("Integer is of type Integer")
        @Test
        public void integerIsOfTypeInteger() {
            assertTrue((Boolean) isOfType.call(context, 12, "Integer"));
        }

        @DisplayName("Integer as String is of type Integer")
        @Test
        public void integerAsStringIsOfTypeInteger() {
            assertTrue((Boolean) isOfType.call(context, "12", "Integer"));
        }

        @DisplayName("Double is not of type Integer")
        @Test
        public void doubleIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Integer"));
        }

        @DisplayName("Double as String is not of type Integer")
        @Test
        public void doubleAsStringIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, "12D", "Integer"));
        }

        @DisplayName("Float is not of type Integer")
        @Test
        public void floatIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Integer"));
        }

        @DisplayName("Float as String is not of type Integer")
        @Test
        public void floatAsStringIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, "12f", "Integer"));
        }

        @DisplayName("Boolean is not of type Integer")
        @Test
        public void booleanIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, true, "Integer"));
        }

        @DisplayName("Boolean as String is not of type Integer")
        @Test
        public void booleanAsStringIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, "true", "Integer"));
        }

        @DisplayName("Date is not of type Integer")
        @Test
        public void dateIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()), "Integer"));
        }

        @DisplayName("Date as String is not of type Integer")
        @Test
        public void dateAsStringIsNotOfTypeInteger() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()).toString(), "Integer"));
        }
    }

    @Nested
    class DoubleType {

        @DisplayName("String is not of type Double")
        @Test
        public void stringIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, "just a String value", "Double"));
        }

        @DisplayName("Integer is of type Double")
        @Test
        public void integerIsOfTypeDouble() {
            assertTrue((Boolean) isOfType.call(context, 12, "Double"));
        }

        @DisplayName("Integer as String is of type Double")
        @Test
        public void integerAsStringIsOfTypeDouble() {
            assertTrue((Boolean) isOfType.call(context, "12", "Double"));
        }

        @DisplayName("Double is of type Double")
        @Test
        public void doubleIsOfTypeDouble() {
            assertTrue((Boolean) isOfType.call(context, 12D, "Double"));
        }

        @DisplayName("Double as String is nnot of type Double")
        @Test
        public void doubleAsStringIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, "12D", "Double"));
        }

        @DisplayName("Float is of type Double")
        @Test
        public void floatIsOfTypeDouble() {
            assertTrue((Boolean) isOfType.call(context, 12f, "Double"));
        }

        @DisplayName("Float as String is not of type Double")
        @Test
        public void floatAsStringIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, "12f", "Double"));
        }

        @DisplayName("Boolean is not of type Double")
        @Test
        public void booleanIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, true, "Double"));
        }

        @DisplayName("Boolean as String is not of type Double")
        @Test
        public void booleanAsStringIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, "true", "Double"));
        }

        @DisplayName("Date is not of type Double")
        @Test
        public void dateIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()), "Double"));
        }

        @DisplayName("Date as String is not of type Double")
        @Test
        public void dateAsStringIsNotOfTypeDouble() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()).toString(), "Double"));
        }
    }

    @Nested
    class BooleanType {

        @DisplayName("String is not of type Boolean")
        @Test
        public void stringIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, "just a String value", "Boolean"));
        }

        @DisplayName("Integer is not of type Boolean")
        @Test
        public void integerIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12, "Boolean"));
        }

        @DisplayName("Integer as String is not of type Boolean")
        @Test
        public void integerAsStringIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, "12", "Boolean"));
        }

        @DisplayName("Double is not of type Boolean")
        @Test
        public void doubleIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Boolean"));
        }

        @DisplayName("Double as String is nnot of type Boolean")
        @Test
        public void doubleAsStringIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, "12D", "Boolean"));
        }

        @DisplayName("Float is not of type Boolean")
        @Test
        public void floatIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Boolean"));
        }

        @DisplayName("Float as String is not of type Boolean")
        @Test
        public void floatAsStringIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, "12f", "Boolean"));
        }

        @DisplayName("Boolean is of type Boolean")
        @Test
        public void booleanIsOfTypeBoolean() {
            assertTrue((Boolean) isOfType.call(context, true, "Boolean"));
        }

        @DisplayName("Boolean as String is of type Boolean")
        @Test
        public void booleanAsStringIsOfTypeBoolean() {
            assertTrue((Boolean) isOfType.call(context, "true", "Boolean"));
        }

        @DisplayName("Date is not of type Boolean")
        @Test
        public void dateIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()), "Boolean"));
        }

        @DisplayName("Date as String is not of type Boolean")
        @Test
        public void dateAsStringIsNotOfTypeBoolean() {
            assertFalse((Boolean) isOfType.call(context, from(Instant.now()).toString(), "Boolean"));
        }
    }

    @Nested
    class DateType {

        @DisplayName("String is not of type Date")
        @Test
        public void stringIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, "just a String value", "Date"));
        }

        @DisplayName("Integer is not of type Date")
        @Test
        public void integerIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, 12, "Date"));
        }

        @DisplayName("Integer as String is not of type Date")
        @Test
        public void integerAsStringIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, "12", "Date"));
        }

        @DisplayName("Double is not of type Date")
        @Test
        public void doubleIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Date"));
        }

        @DisplayName("Double as String is nnot of type Date")
        @Test
        public void doubleAsStringIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, "12D", "Date"));
        }

        @DisplayName("Float is not of type Date")
        @Test
        public void floatIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Date"));
        }

        @DisplayName("Float as String is not of type Date")
        @Test
        public void floatAsStringIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, "12f", "Date"));
        }

        @DisplayName("Boolean is not of type Date")
        @Test
        public void booleanIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, true, "Date"));
        }

        @DisplayName("Boolean as String is not of type Date")
        @Test
        public void booleanAsStringIsNotOfTypeDate() {
            assertFalse((Boolean) isOfType.call(context, "true", "Date"));
        }

        @DisplayName("Date is of type Date")
        @Test
        public void dateIsOfTypeDate() {
            assertTrue((Boolean) isOfType.call(context, from(Instant.now()), "Date"));
        }

        @DisplayName("Date as String is of type Date")
        @Test
        public void dateAsStringIsOfTypeDate() {
            assertTrue((Boolean) isOfType.call(context, from(Instant.now()).toString(), "Date"));
        }
    }

    @DisplayName("Custom 'isOfType' Function is called via the DSEL interpreter")
    @Test
    public void isCalledViaDSELInterpreter() {
        testEvalExpression(true, "isOfType('2022-09-26 11:36:31.179', 'DaTE')");
        testEvalExpression(false, "isOfType('2022-09-26 11:36:31.179', 'BOoLEan')");
    }

    @DisplayName("Custom 'isOfType' Function is called with throwing Exception via the DSEL interpreter")
    @Test
    public void isCalledWithThrowingExceptionViaDSELInterpreter() {
        testEvalExpressionThrowsFunctionException("isOfType()");
        testEvalExpressionThrowsFunctionException("isOfType('113218')");
        testEvalExpressionThrowsFunctionException("isOfType('2022-10-04 09:12:02.918', '', '')");
        testEvalExpressionThrowsFunctionException("isOfType('2901', INTEGER)");
        testEvalExpressionThrowsFunctionException("isOfType('2901', 'integer', 'integer')");
    }

    private void setUpContextWithIntegerCategory() {
        SemanticProperties properties = new SemanticProperties(tempDir.toString());
        CategoryRegistryManager categoryRegistryManager = new CategoryRegistryManager(properties);
        DeletableDictionarySnapshotOpener opener = new DeletableDictionarySnapshotOpener(properties,
                categoryRegistryManager.getSharedDictionary());
        DQCategory integerCategory = createIntegerCategory();
        CustomDictionaryHolder holder = categoryRegistryManager.getCustomDictionaryHolder("fakeTenantId");

        holder.createCategory(integerCategory);
        dictionarySnapshot = opener.openDeletableDictionarySnapshot("fakeTenantId");

        HPathStore store = new DselHPathStore();
        store.put("dictionarySnapshot", dictionarySnapshot);

        context = new ExprLangContext();
        context.setStore(store);
    }

    private DQCategory createIntegerCategory() {
        return DQCategory.newBuilder().id("1").name("INTEGER").label("Integer").type(CategoryType.REGEX)
                .regEx(DQRegEx.newBuilder().validator(DQValidator.newBuilder().patternString("[0-9te]") // integer notation in
                                                                                                        // music theory
                        .build()).mainCategory(MainCategory.AlphaNumeric).build())
                .completeness(true).build();
    }
}
