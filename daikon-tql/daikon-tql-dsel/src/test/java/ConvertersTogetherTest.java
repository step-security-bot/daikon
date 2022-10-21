import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.Tql;
import org.talend.tqldsel.dseltotql.DselToTqlConverter;
import org.talend.tqldsel.tqltodsel.TqlToDselConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ConvertersTogetherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertersTogetherTest.class);

    @Test
    public void testParseLiteralComparisonEqForString() {
        callConvertors("(field1 = 'abc')");
    }

    @Test
    public void testParseLiteralComparisonEqForBool() {
        callConvertors("(field1 = false)");
    }

    @Test
    public void testParseLiteralComparisonEqForInt() {
        callConvertors("(field1 = 123)");
    }

    @Test
    public void testParseLiteralComparisonNeqForString() {
        callConvertors("(field1 != 'abc')");
    }

    @Test
    public void testParseLiteralComparisonNeqForBool() {
        callConvertors("(field1 != true)");
    }

    @Test
    public void testParseLiteralComparisonNeqForInt() {
        callConvertors("(field1 != 73871)");
    }

    @Test
    public void testParseLiteralComparisonLtForInt() {
        callConvertors("(field1 < 91)");
    }

    @Test
    public void testParseLiteralComparisonLtForDecimal() {
        callConvertors("(field1 < 98.183)");
    }

    @Disabled("testParseTwoFieldComparisonLt() test currently disabled because an issue has occurred with it, need investigations")
    @Test
    public void testParseTwoFieldComparisonLt() {
        callConvertors("(field1 < field2)");
    }

    @Test
    public void testParseLiteralComparisonGtForInt() {
        callConvertors("(field1 > 312)");
    }

    @Test
    public void testParseLiteralComparisonGtForDecimal() {
        callConvertors("(field1 > 555.72)");
    }

    @Disabled("testParseTwoFieldComparisonGt() test currently disabled because an issue has occurred with it, need investigations")
    @Test
    public void testParseTwoFieldComparisonGt() {
        callConvertors("(field1 > field2)");
    }

    @Test
    public void testParseLiteralComparisonLetForInt() {
        callConvertors("(field1 <= 7)");
    }

    @Test
    public void testParseLiteralComparisonLetForDecimal() {
        callConvertors("(field1 <= 9821.1972)");
    }

    @Disabled("testParseTwoFieldComparisonLet() test currently disabled because an issue has occurred with it, need investigations")
    @Test
    public void testParseTwoFieldComparisonLet() {
        callConvertors("(field1 <= field2)");
    }

    @Test
    public void testParseLiteralComparisonGetForInt() {
        callConvertors("(field1 >= 10931)");
    }

    @Test
    public void testParseLiteralComparisonGetForDecimal() {
        callConvertors("(field1 >= 2.091)");
    }

    @Disabled("testParseTwoFieldComparisonGte() test currently disabled because an issue has occurred with it, need investigations")
    @Test
    public void testParseTwoFieldComparisonGet() {
        callConvertors("(field1 >= field2)");
    }

    @Test
    public void testParseContains() {
        callConvertors("(field1 contains 'hello')");
    }

    @Test
    public void testParseContainsForRuntime() {
        callConvertors("(field1 contains 'hello')", Collections.emptyMap(), true);
    }

    @Test
    public void testParseContainsIgnoreCase() {
        callConvertors("(field1 containsIgnoreCase 'HEllO')");
    }

    @Test
    public void testParseBetweenFunctionForString() {
        callConvertors("(field1 between ['value1', 'value2'])");
    }

    @Test
    public void testParseBetweenFunctionForInt() {
        callConvertors("(field1 between [3, 621])");
    }

    @Test
    public void testParseBetweenFunctionForDecimal() {
        callConvertors("(field1 between [5.972, 991.27])");
    }

    @Test
    public void testParseBetweenFunctionForLong() {
        callConvertors("(field1 between [1665577759000, 1705577759000])");
    }

    @Test
    public void testParseAnd() {
        callConvertors("(field1 = 123) and (field2 < 124) and (field3 > 125)");
    }

    @Test
    public void testParseOr() {
        callConvertors("(field1 = 123) or (field2 < 124) or (field3 > 125)");
    }

    @Test
    public void testParseAndOr() {
        callConvertors("((field1 = 123) and (field2 < 124)) or ((field3 > 125) and (field4 <= 126))");
    }

    @Test
    public void testParseInForInt() {
        callConvertors("(field1 in [89178, 12, 99, 2])");
    }

    @Test
    public void testParseInForString() {
        callConvertors("(field1 in ['value1', 'value2'])");
    }

    @Test
    public void testParseInForDecimal() {
        callConvertors("(field1 in [525.87, 12, 99.20, 252.0])");
    }

    @Test
    public void testParseFieldComplies() {
        callConvertors("(name complies 'Aaa Aaaa')");
    }

    @Test
    public void testParseFieldMatchesRegex() {
        callConvertors("(name ~ '^[A-Z][a-z]*$')");
    }

    @Test
    public void testParseFieldWordComplies() {
        callConvertors("(name wordComplies '[Word] [word][digit]')");
    }

    @Test
    public void testParseIsEmpty() {
        callConvertors("(field1 is empty)");
    }

    @Test
    public void testParseIsEmptyWithAllFields() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        fToType.put("accountNumber", "INTEGER");
        callConvertors("(* is empty)", fToType, false);
    }

    @Test
    public void testParseIsEmptyWithAllFieldsForRuntime() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        fToType.put("accountNumber", "INTEGER");
        callConvertors("(* is empty)", fToType, true);
    }

    @Test
    public void testParseIsValidWithField() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        callConvertors("(field1 is valid)", fToType, false);
    }

    @Test
    public void testParseIsInvalidWithField() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        callConvertors("(field1 is invalid)", fToType, false);
    }

    @Test
    public void testParseIsInvalidWithAllFields() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        fToType.put("accountNumber", "INTEGER");
        callConvertors("(* is invalid)", fToType, false);
    }

    @Test
    public void testParseIsInvalidWithAllFieldsForRuntime() {
        final Map<String, String> fToType = new HashMap<>();
        fToType.put("field1", "STRING");
        fToType.put("accountNumber", "INTEGER");
        callConvertors("(* is invalid)", fToType, true);
    }

    @Test
    public void testParseIsNull() {
        callConvertors("(field1 is null)");
    }

    @Test
    public void testParseNot() {
        callConvertors("(not ((field1 = 'value1')))");
    }

    @Test
    public void testParseLogicalWithParenthesis() {
        callConvertors("((firstName = 'John') and (lastName = 'Doe')) or (firstName = 'Jacques')");
    }

    @Test
    public void testParseLogicalWithParenthesisForPrecedenceOnOr() {
        callConvertors("((firstName = 'John') and (lastName = 'Doe')) or ((firstName = 'Jacques') and (lastName = 'Dupond'))");
    }

    private void callConvertors(final String tqlQuery) {
        callConvertors(tqlQuery, Collections.emptyMap(), false);
    }

    private void callConvertors(final String tqlQuery, final Map<String, String> fieldToType, final boolean isForRuntime) {
        LOGGER.debug("- Original TQL query : " + tqlQuery);

        final TqlElement tqlElement = Tql.parse(tqlQuery);
        LOGGER.debug("- Original TQL Element from TQL query : " + tqlElement);

        final ELNode elNode;

        if (isForRuntime) {
            elNode = TqlToDselConverter.convertForRuntime(tqlQuery, fieldToType);
        } else {
            elNode = TqlToDselConverter.convertForDb(tqlQuery, fieldToType);
        }

        LOGGER.debug("- DSEL ELNode converted from TQL query : " + elNode);

        final TqlElement convertedTqlQueryFromELNode = DselToTqlConverter.convert(elNode);
        LOGGER.debug("- AST of converted TQL query from DSEL ELNode : " + convertedTqlQueryFromELNode.toString());

        final String TEST_TAB = "-";
        final String dselQueryAsPretty = new ELNodePrinter(TEST_TAB, false).prettyPrint(elNode);
        LOGGER.debug("- Pretty DSEL query converted from DSEL ELNode from TQL query : " + dselQueryAsPretty);

        final TqlElement convertedTqlQueryFromDselQuery = DselToTqlConverter.convert(dselQueryAsPretty);
        LOGGER.debug("- AST of converted TQL query from DSEL query : " + convertedTqlQueryFromDselQuery.toString());

        final String tqlQueryFromDselQuery = convertedTqlQueryFromDselQuery.toQueryString();
        LOGGER.debug("- Converted TQL query as String representation from DSEL query : " + tqlQueryFromDselQuery);

        if (isForRuntime) {
            assertNotEquals(tqlQuery, tqlQueryFromDselQuery);
        } else {
            assertEquals(tqlQuery, tqlQueryFromDselQuery);
        }
    }
}
