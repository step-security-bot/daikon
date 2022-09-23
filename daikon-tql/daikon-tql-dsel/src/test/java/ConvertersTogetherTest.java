import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.Tql;
import org.talend.tqldsel.dseltotql.DselToTqlConverter;
import org.talend.tqldsel.tqltodsel.TqlToDselConverter;

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
    public void testParseLiteralComparisonLtForDouble() {
        callConvertors("(field1 < 98.183)");
    }

    // TODO : issue here, need investigations :
    @Disabled
    public void testParseTwoFieldComparisonLt() {
        callConvertors("(field1 < field2)");
    }

    @Test
    public void testParseLiteralComparisonGtForInt() {
        callConvertors("(field1 > 312)");
    }

    @Test
    public void testParseLiteralComparisonGtForDouble() {
        callConvertors("(field1 > 555.72)");
    }

    // TODO : issue here, need investigations :
    @Disabled
    public void testParseTwoFieldComparisonGt() {
        callConvertors("(field1 > field2)");
    }

    @Test
    public void testParseLiteralComparisonLetForInt() {
        callConvertors("(field1 <= 7)");
    }

    @Test
    public void testParseLiteralComparisonLetForDouble() {
        callConvertors("(field1 <= 9821.1972)");
    }

    // TODO : issue here, need investigations
    @Disabled
    public void testParseTwoFieldComparisonLet() {
        callConvertors("(field1 <= field2)");
    }

    @Test
    public void testParseLiteralComparisonGetForInt() {
        callConvertors("(field1 >= 10931)");
    }

    @Test
    public void testParseLiteralComparisonGetForDouble() {
        callConvertors("(field1 >= 2.091)");
    }

    // TODO : issue here, need investigations
    @Disabled
    public void testParseTwoFieldComparisonGet() {
        callConvertors("(field1 >= field2)");
    }

    @Test
    public void testParseContains() {
        callConvertors("(field1 contains 'hello')");
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
    public void testParseBetweenFunctionForDouble() {
        callConvertors("(field1 between [5.972, 991.27])");
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
    public void testParseInForDouble() {
        callConvertors("(field1 in [525.87, 12, 99.20, 252.0])");
    }

    // TODO : Issue here, because the char pattern is converted to a regex and currently a regex
    // TODO can't be reverted to a char pattern.
    @Disabled
    public void testParseFieldComply() throws Exception {
        callConvertors("(name complies 'Aaa Aaaa')");
    }

    @Test
    public void testParseFieldMatchesRegex() throws Exception {
        callConvertors("(name ~ '^[A-Z][a-z]*$')");
    }

    // TODO : Issue here, because the char pattern is converted to a regex and currently a regex
    // TODO can't be reverted to a char pattern.
    @Disabled
    public void testParseFieldWordComplies() throws Exception {
        callConvertors("(name wordComplies '[Word] [word][digit]')");
    }

    @Test
    public void testParseIsEmpty() {
        callConvertors("(field1 is empty)");
    }

    @Test
    public void testParseIsValidFailsBecauseNotImplemented() {
        callConvertorsAndFailsWithTqlException("field1 is valid)");
    }

    @Test
    public void testParseIsInvalidFailsBecauseNotImplemented() {
        callConvertorsAndFailsWithTqlException("field1 is invalid)");
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
        LOGGER.debug("- Original TQL query : " + tqlQuery);

        final TqlElement tqlElement = Tql.parse(tqlQuery);
        LOGGER.debug("- Original TQL Element from TQL query : " + tqlElement);

        final ELNode elNode = TqlToDselConverter.convert(tqlQuery);
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

        assertEquals(tqlQuery, tqlQueryFromDselQuery);
    }

    private void callConvertorsAndFailsWithTqlException(final String tqlQuery) {
        LOGGER.debug("- Original TQL query : " + tqlQuery);

        final TqlElement tqlElement = Tql.parse(tqlQuery);
        LOGGER.debug("- Original TQL Element from TQL query : " + tqlElement);

        assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery));
    }
}
