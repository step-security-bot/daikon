package org.talend.dsel.functions.custom;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.time.Instant;
import java.util.Date;

import org.joda.time.DateTime;
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

        @DisplayName("Integer category not prevalent on Integer native type")
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
    class Integer {

        @DisplayName("String int is of type Integer")
        @Test
        public void intStringIsInteger() {
            assertTrue((Boolean) isOfType.call(context, "12", "Integer"));
        }

        @DisplayName("int is of type Integer")
        @Test
        public void intIsInteger() {
            assertTrue((Boolean) isOfType.call(context, 12, "Integer"));
        }

        @DisplayName("String double is not of type Integer")
        @Test
        public void intDoubleNotInteger() {
            assertFalse((Boolean) isOfType.call(context, "12.0", "Integer"));
        }

        @DisplayName("long is of type Integer")
        @Test
        public void longIsInteger() {
            assertTrue((Boolean) isOfType.call(context, 12L, "Integer"));
        }

        @DisplayName("double is not of type Integer")
        @Test
        public void doubleNotInteger() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Integer"));
        }

        @DisplayName("float is not of type Integer")
        @Test
        public void floatNotInteger() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Integer"));
        }

        @DisplayName("boolean is not of type Integer")
        @Test
        public void booleanNotInteger() {
            assertFalse((Boolean) isOfType.call(context, true, "Integer"));
        }

        @DisplayName("date is not of type Integer")
        @Test
        public void dateNotInteger() {
            assertFalse((Boolean) isOfType.call(context, Date.from(Instant.now()), "Integer"));
        }
    }

    @Nested
    class Decimal {

        @DisplayName("String int is of type Decimal")
        @Test
        public void intStringIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, "12", "Decimal"));
        }

        @DisplayName("String double is of type Decimal")
        @Test
        public void doubleStringIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, "12.0", "Decimal"));
        }

        @DisplayName("int is of type Decimal")
        @Test
        public void intIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, 12, "Decimal"));
        }

        @DisplayName("long is of type Decimal")
        @Test
        public void longIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, 12L, "Decimal"));
        }

        @DisplayName("double is of type Decimal")
        @Test
        public void doubleIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, 12D, "Decimal"));
        }

        @DisplayName("float is of type Decimal")
        @Test
        public void floatIsDecimal() {
            assertTrue((Boolean) isOfType.call(context, 12f, "Decimal"));
        }

        @DisplayName("boolean is not of type Decimal")
        @Test
        public void booleanNotDecimal() {
            assertFalse((Boolean) isOfType.call(context, true, "Decimal"));
        }

        @DisplayName("date is not of type Decimal")
        @Test
        public void dateNotDecimal() {
            assertFalse((Boolean) isOfType.call(context, Date.from(Instant.now()), "Decimal"));
        }
    }

    @Nested
    class NativeDate {

        @DisplayName("String int is not of type Date")
        @Test
        public void intStringNotDate() {
            assertFalse((Boolean) isOfType.call(context, "12", "Date"));
        }

        @DisplayName("String double is not of type Date")
        @Test
        public void doubleStringNotDate() {
            assertFalse((Boolean) isOfType.call(context, "12.0", "Date"));
        }

        @DisplayName("String date is of type Date")
        @Test
        public void doubleStringIsDate() {
            assertTrue((Boolean) isOfType.call(context, "2020-12-31", "Date"));
        }

        @DisplayName("int is not of type Date")
        @Test
        public void intNotDate() {
            assertFalse((Boolean) isOfType.call(context, 12, "Date"));
        }

        @DisplayName("long is not of type Date")
        @Test
        public void longNotDate() {
            assertFalse((Boolean) isOfType.call(context, 12L, "Date"));
        }

        @DisplayName("double is not of type Date")
        @Test
        public void doubleNotDate() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Date"));
        }

        @DisplayName("float is not of type Date")
        @Test
        public void floatNotDate() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Date"));
        }

        @DisplayName("boolean is not of type Date")
        @Test
        public void booleanNotDate() {
            assertFalse((Boolean) isOfType.call(context, true, "Date"));
        }

        @DisplayName("date is of type Date")
        @Test
        public void dateIsDate() {
            assertTrue((Boolean) isOfType.call(context, Date.from(Instant.now()), "Date"));
        }

        @DisplayName("datetime is of type Date")
        @Test
        public void datetimeIsDate() {
            assertTrue((Boolean) isOfType.call(context, DateTime.now(), "Date"));
        }

        @DisplayName("time is not of type Date")
        @Test
        public void timeNotDate() {
            assertFalse((Boolean) isOfType.call(context, DateTime.now().toLocalTime(), "Date"));
        }
    }

    @Nested
    class NativeBoolean {

        @DisplayName("String int is not of type Boolean")
        @Test
        public void intStringNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, "12", "Boolean"));
        }

        @DisplayName("String double is not of type Boolean")
        @Test
        public void doubleStringNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, "12.0", "Boolean"));
        }

        @DisplayName("String boolean is of type Boolean")
        @Test
        public void booleanStringIsBoolean() {
            assertTrue((Boolean) isOfType.call(context, "true", "Boolean"));
        }

        @DisplayName("int is not of type Boolean")
        @Test
        public void intNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12, "Boolean"));
        }

        @DisplayName("long is not of type Boolean")
        @Test
        public void longNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12L, "Boolean"));
        }

        @DisplayName("double is not of type Boolean")
        @Test
        public void doubleNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12D, "Boolean"));
        }

        @DisplayName("float is not of type Boolean")
        @Test
        public void floatNotBoolean() {
            assertFalse((Boolean) isOfType.call(context, 12f, "Boolean"));
        }

        @DisplayName("boolean is of type Boolean")
        @Test
        public void booleanIsBoolean() {
            assertTrue((Boolean) isOfType.call(context, true, "Boolean"));
        }

        @DisplayName("date is not of type Boolean")
        @Test
        public void dateIsBoolean() {
            assertFalse((Boolean) isOfType.call(context, Date.from(Instant.now()), "Boolean"));
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
