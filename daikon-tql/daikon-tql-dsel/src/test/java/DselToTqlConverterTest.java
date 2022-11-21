import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.tql.api.TqlBuilder.and;
import static org.talend.tql.api.TqlBuilder.between;
import static org.talend.tql.api.TqlBuilder.contains;
import static org.talend.tql.api.TqlBuilder.containsIgnoreCase;
import static org.talend.tql.api.TqlBuilder.eq;
import static org.talend.tql.api.TqlBuilder.gt;
import static org.talend.tql.api.TqlBuilder.gtFields;
import static org.talend.tql.api.TqlBuilder.gte;
import static org.talend.tql.api.TqlBuilder.gteFields;
import static org.talend.tql.api.TqlBuilder.in;
import static org.talend.tql.api.TqlBuilder.isEmpty;
import static org.talend.tql.api.TqlBuilder.isNull;
import static org.talend.tql.api.TqlBuilder.lt;
import static org.talend.tql.api.TqlBuilder.ltFields;
import static org.talend.tql.api.TqlBuilder.lte;
import static org.talend.tql.api.TqlBuilder.lteFields;
import static org.talend.tql.api.TqlBuilder.match;
import static org.talend.tql.api.TqlBuilder.neq;
import static org.talend.tql.api.TqlBuilder.not;
import static org.talend.tql.api.TqlBuilder.or;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.ExprLangException;
import org.talend.tql.TqlLexer;
import org.talend.tql.TqlParser;
import org.talend.tql.model.AndExpression;
import org.talend.tql.model.Expression;
import org.talend.tql.model.FieldBetweenExpression;
import org.talend.tql.model.FieldReference;
import org.talend.tql.model.LiteralValue;
import org.talend.tql.model.OrExpression;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.TqlExpressionVisitor;
import org.talend.tqldsel.dseltotql.DselToTqlConverter;

public class DselToTqlConverterTest {

    @Test
    public void testParseLiteralComparisonEqForString() {
        final String dselQuery = "field1=='123'";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", "123");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqForBool() {
        final String dselQuery = "field1==true";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", true);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqForInt() {
        final String dselQuery = "field1 == 123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqForDouble() {
        final String dselQuery = "field1 ==999d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", 999.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqForDecimal() {
        final String dselQuery = "field1 ==123.456";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", 123.456);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqWithNegative() {
        final String dselQuery = "field1=-123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", "-123");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonEqWithParenthesis() {
        final String dselQuery = "(((field1 = 123)))";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = eq("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonNeqForString() {
        final String dselQuery = "field1!= '123'";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = neq("field1", "123");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonNeqForBool() {
        final String dselQuery = "field1!=true";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = neq("field1", true);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonNeqForInt() {
        final String dselQuery = "field1 !=123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = neq("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonNeqForDouble() {
        final String dselQuery = "field1 != 1231d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = neq("field1", 1231.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonNeqForDecimal() {
        final String dselQuery = "field1 != 817.2372";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = neq("field1", 817.2372);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLtForInt() {
        final String dselQuery = "field1< 123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lt("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLtForDouble() {
        final String dselQuery = "field1 <01912d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lt("field1", 1912.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLtForDecimal() {
        final String dselQuery = "field1 <1091.328";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lt("field1", 1091.328);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Disabled("testParseTwoFieldComparisonLt() test currently disabled because an issue has occurred with it, need investigations")
    public void testParseFieldComparisonLt() {
        final String dselQuery = "field1< field2";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = ltFields("field1", "field2");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGtForInt() {
        final String dselQuery = "field1>123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gt("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGtForDouble() {
        final String dselQuery = "field1>1111d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gt("field1", 1111.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGtForDecimal() {
        final String dselQuery = "field1>91.1723";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gt("field1", 91.1723);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Disabled("testParseTwoFieldComparisonGt() test currently disabled because an issue has occurred with it, need investigations")
    public void testParseTwoFieldsComparisonGt() {
        final String dselQuery = "field1> field2";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gtFields("field1", "field2");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLetForInt() {
        final String dselQuery = "field1<=123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lte("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLetForDouble() {
        final String dselQuery = "field1<=911d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lte("field1", 911.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonLetForDecimal() {
        final String dselQuery = "field1<=61891.2191";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lte("field1", 61891.2191);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Disabled("testParseTwoFieldComparisonLet() test currently disabled because an issue has occurred with it, need investigations")
    public void testParseTwoFieldComparisonLet() {
        final String dselQuery = "field1 <= field2";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = lteFields("field1", "field2");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGetForInt() {
        final String dselQuery = "field1>=123";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gte("field1", 123);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGetForDecimal() {
        final String dselQuery = "field1>=1.192378";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gte("field1", 1.192378);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseLiteralComparisonGetForDouble() {
        final String dselQuery = "field1>=77112d";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gte("field1", 77112.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Disabled("testParseTwoFieldComparisonGet() test currently disabled because an issue has occurred with it, need investigations")
    public void testParseTwoFieldsComparisonGet() {
        final String dselQuery = "champ1 >= champ2";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = gteFields("champ1", "champ2");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseContains() {
        final String dselQuery = "contains(field1, '123')";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = contains("field1", "'123'");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseContainsIgnoringCase() {
        final String dselQuery = "contains(field1, 'aBcDE', true)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = containsIgnoreCase("field1", "'aBcDE'");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseBetweenForInt() {
        final String dselQuery = "between(field1, 123, 789)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = between("field1", 123, 789);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseBetweenForDouble() {
        final String dselQuery = "between(field1, 123d, 789d, true)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);

        LiteralValue left = new LiteralValue(LiteralValue.Enum.DECIMAL, "123.0");
        LiteralValue right = new LiteralValue(LiteralValue.Enum.DECIMAL, "789.0");
        FieldBetweenExpression fieldBetweenExpression = new FieldBetweenExpression(new FieldReference("field1"), left, right,
                true, false);
        Expression[] fieldBetweenExpressions = new Expression[] { fieldBetweenExpression };

        // Adding it to a new AST
        AndExpression andExpression = new AndExpression(fieldBetweenExpressions);
        final TqlElement expectedTqlQuery = new OrExpression(andExpression);

        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseBetweenForMix() {
        final String dselQuery = "between(field1, 9187.1892, 789d, false, false)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);

        LiteralValue left = new LiteralValue(LiteralValue.Enum.DECIMAL, "9187.1892");
        LiteralValue right = new LiteralValue(LiteralValue.Enum.DECIMAL, "789.0");
        FieldBetweenExpression fieldBetweenExpression = new FieldBetweenExpression(new FieldReference("field1"), left, right,
                false, false);
        Expression[] fieldBetweenExpressions = new Expression[] { fieldBetweenExpression };

        // Adding it to a new AST
        AndExpression andExpression = new AndExpression(fieldBetweenExpressions);
        final TqlElement expectedTqlQuery = new OrExpression(andExpression);

        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseBetweenForDecimal() {
        final String dselQuery = "between(field1, 9187.1892, 789.0, true, true)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);

        LiteralValue left = new LiteralValue(LiteralValue.Enum.DECIMAL, "9187.1892");
        LiteralValue right = new LiteralValue(LiteralValue.Enum.DECIMAL, "789.0");
        FieldBetweenExpression fieldBetweenExpression = new FieldBetweenExpression(new FieldReference("field1"), left, right,
                true, true);
        Expression[] fieldBetweenExpressions = new Expression[] { fieldBetweenExpression };

        // Adding it to a new AST
        AndExpression andExpression = new AndExpression(fieldBetweenExpressions);
        final TqlElement expectedTqlQuery = new OrExpression(andExpression);

        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseBetweenForString() {
        final String dselQuery = "between(field1, '123', '789', false, true)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);

        LiteralValue left = new LiteralValue(LiteralValue.Enum.QUOTED_VALUE, "123");
        LiteralValue right = new LiteralValue(LiteralValue.Enum.QUOTED_VALUE, "789");
        FieldBetweenExpression fieldBetweenExpression = new FieldBetweenExpression(new FieldReference("field1"), left, right,
                false, true);
        Expression[] fieldBetweenExpressions = new Expression[] { fieldBetweenExpression };

        // Adding it to a new AST
        AndExpression andExpression = new AndExpression(fieldBetweenExpressions);
        final TqlElement expectedTqlQuery = new OrExpression(andExpression);

        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseInForInt() {
        final String dselQuery = "in(field1, 1, 2, 3, 4)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = in("field1", 1, 2, 3, 4);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseInForDouble() {
        final String dselQuery = "in(field1, 1d, 2d, 3d, 4d)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = in("field1", 1.0, 2.0, 3.0, 4.0);
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseInForString() {
        final String dselQuery = "in(field1, '1', '2', '3', '4')";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = in("field1", "1", "2", "3", "4");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseEmpty() {
        final String dselQuery = "isEmpty(field1)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = isEmpty("field1");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseMatches() {
        final String dselQuery = "matches(field1, '\\d+')";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = match("field1", "'\\d+'");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseIsNull() {
        final String dselQuery = "isNull(field1)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = isNull("field1");
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseNotIsNull() {
        final String dselQuery = "!(isNull(field1))";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = not(isNull("field1"));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseNotByOperator() {
        final String dselQuery = "!(field1 == true)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = not(eq("field1", true));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseNotByWord() {
        final String dselQuery = "not(field1 == false)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = not(eq("field1", false));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseAnd() {
        final String dselQuery = "(field1 = 1) && (field2 = 2)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = and(eq("field1", 1), eq("field2", 2));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseOr() {
        final String dselQuery = "(field1 = 1) || (field2 = 2)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = or(eq("field1", 1), eq("field2", 2));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseNotByOperatorForLiteralIsThrowingException() {
        assertThrows(IllegalArgumentException.class, () -> DselToTqlConverter.convert("!(field1)"));
    }

    @Test
    public void testParsePlusOperatorIsThrowingException() {
        assertTqlElementThrowsIllegalStateException("2 + 3");
        assertTqlElementThrowsIllegalStateException("3 - 2");
    }

    @Test
    public void testParseMultiOperatorIsThrowingException() {
        assertTqlElementThrowsIllegalStateException("3 * 4");
        assertTqlElementThrowsIllegalStateException("4 / 2");
    }

    @Test
    public void testParseIfThenElseIsThrowingException() {
        assertTqlElementThrowsIllegalStateException("if (condition1) expression1 else expression2");
        assertTqlElementThrowsIllegalStateException("if (condition) {expression1, expression2} else expression3");
        assertTqlElementThrowsIllegalStateException(
                "if (condition1) expression1 elseif (condition2) expression2 else expression3");
        assertTqlElementThrowsIllegalStateException(
                "if (condition) {identifier1 = expression1} else {identifier2 = expression2}");
    }

    @Test
    public void testParseSwitchCaseIsThrowingException() {
        assertTqlElementThrowsIllegalStateException(
                "switch (field1) { case 'yes': expression2, expression3 case 'no': expression4, expression5 default: expression6}");
    }

    @Test
    public void testParseComplex1() {
        final String dselQuery = "not(field1=='26') || (field3==198d)";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = or(not(eq("field1", "26")), eq("field3", 198d));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testParseComplex2() {
        final String dselQuery = "(not(field1==true) || (field3>=153 && matches(field1, '\\d+') || in(field1, 1d, 2d, 3d, 4d))) && !isNull(field4) && contains(field5, '123')";
        final TqlElement convertedTqlQuery = DselToTqlConverter.convert(dselQuery);
        final TqlElement expectedTqlQuery = and(
                or(not(eq("field1", true)), or(and(gte("field3", 153), match("field1", "'\\d+'")), in("field1", 1d, 2d, 3d, 4d))),
                not(isNull("field4")), contains("field5", "'123'"));
        assertTqlElementsAreEqualsAndExecutionIsOK(convertedTqlQuery, expectedTqlQuery);
    }

    @Test
    public void testArrayIsThrowingException() {
        assertThrows(ExprLangException.class, () -> DselToTqlConverter.convert("[1, 4, 583, 1918]"));
    }

    private static void assertTqlElementsAreEqualsAndExecutionIsOK(final TqlElement convertedTqlQuery,
            final TqlElement expectedTqlQuery) {
        assertEquals(expectedTqlQuery.toString(), convertedTqlQuery.toString());

        CharStream input = CharStreams.fromString(expectedTqlQuery.toQueryString());
        TqlLexer lexer = new TqlLexer(input);
        TqlParser parser = new TqlParser(new CommonTokenStream(lexer));
        TqlParser.ExpressionContext expression = parser.expression();
        assertDoesNotThrow(() -> expression.accept(new TqlExpressionVisitor()),
                "TQL expression should not throw an exception because it should be valid");
    }

    private void assertTqlElementThrowsIllegalStateException(final String dselQuery) {
        assertThrows(IllegalStateException.class, () -> DselToTqlConverter.convert(dselQuery));
    }
}
