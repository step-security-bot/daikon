import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.tqldsel.tqltodsel.TqlToDselConverter.wrapNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;
import org.talend.tql.parser.Tql;
import org.talend.tqldsel.tqltodsel.TqlToDselConverter;

public class TqlToDselConverterTest {

    static Map<String, String> fieldToType;

    @BeforeAll
    static void setUp() {
        final HashMap<String, String> fToType = new HashMap<>();
        fToType.put("name", "STRING");
        fToType.put("total", "INTEGER");
        fToType.put("isActivated", "BOOLEAN");
        fToType.put("money", "DECIMAL");
        fToType.put("special", "semanticType1");
        fieldToType = Collections.unmodifiableMap(fToType);
    }

    @Test
    public void testParseSingleNot() {
        final String query = "not (field1=123 and field2<124)";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChildren(lst);
        ELNode expected = new ELNode(ELNodeType.NOT, "!");
        expected.addChild(andNode);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsNullWithTqlExpression() {
        final String tqlQuery = "field1 is null";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convertForDb(tqlExpression);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", null);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsNull() {
        final String tqlQuery = "field1 is null";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", null);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseNotIsNull() {
        final String tqlQuery = "not(field1 is null)";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", null);
        ELNode expected = new ELNode(ELNodeType.NOT, "!");
        expected.addChild(nullNode);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsEmpty() {
        final String tqlQuery = "field1 is empty";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", null);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsEmptyAnd() {
        final String tqlQuery = "(field1 is empty) and ((field2 is null))";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", null);
        ELNode isNull = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field2", null);
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChild(isEmptyNode);
        expected.addChild(isNull);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsEmptyWithAllFields() {
        final String tqlQuery = "* is empty";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "hasEmpty");
        expected.addChild(new ELNode(ELNodeType.HPATH, "'*'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsEmptyWithAllFieldsForRuntime() {
        final String tqlQuery = "* is empty";
        ELNode actual = TqlToDselConverter.convertForRuntime(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.OR, "||");

        ELNode isInvalidNode1 = new ELNode(ELNodeType.FUNCTION_CALL, "isEmpty");
        isInvalidNode1.addChild(new ELNode(ELNodeType.STRING_LITERAL, "special"));

        expected.addChild(isInvalidNode1);

        ELNode isInvalidNode2 = new ELNode(ELNodeType.FUNCTION_CALL, "isEmpty");
        isInvalidNode2.addChild(new ELNode(ELNodeType.STRING_LITERAL, "total"));

        expected.addChild(isInvalidNode2);

        ELNode isInvalidNode3 = new ELNode(ELNodeType.FUNCTION_CALL, "isEmpty");
        isInvalidNode3.addChild(new ELNode(ELNodeType.STRING_LITERAL, "money"));
        expected.addChild(isInvalidNode3);

        ELNode isInvalidNode4 = new ELNode(ELNodeType.FUNCTION_CALL, "isEmpty");
        isInvalidNode4.addChild(new ELNode(ELNodeType.STRING_LITERAL, "name"));
        expected.addChild(isInvalidNode4);

        ELNode isInvalidNode5 = new ELNode(ELNodeType.FUNCTION_CALL, "isEmpty");
        isInvalidNode5.addChild(new ELNode(ELNodeType.STRING_LITERAL, "isActivated"));
        expected.addChild(isInvalidNode5);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseTqlLiteralWithSingleQuote() {
        final String tqlQuery = "name = 'abc\\'def'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.EQUAL);
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'abc\\'def'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseRegEx() {
        final String tqlQuery = "name ~ '^[A-Z][a-z]*$'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'^[A-Z][a-z]*$'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseRegEx2() {
        final String tqlQuery = "name ~ '\\d'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'\\d'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseRegExWithSingleQuote() {
        final String tqlQuery = "name ~ '\\d\\'\\w'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'\\d\\'\\w'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseTqlWordComplies() {
        final String tqlQuery = "name wordComplies '[word]'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "wordComplies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'[word]'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseTqlWordCompliesWithSingleQuote() {
        final String tqlQuery = "name wordComplies '[word]\\'[word]'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "wordComplies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'[word]\\'[word]'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseTqlComplies() {
        final String tqlQuery = "name complies 'aaa'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "complies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'aaa'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseTqlCompliesWithSingleQuote() {
        final String tqlQuery = "name complies 'aaa\\'aaa'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "complies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'aaa\\'aaa'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseContains() {
        final String tqlQuery = "name contains 'am'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'am'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseContainsWithSingleQuote() {
        final String tqlQuery = "name contains 'I\\'m'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'I\\'m'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseContainsWithExpectedException() {
        final String tqlQuery = "name contains";
        assertThrows(TqlException.class, () -> TqlToDselConverter.convertForDb(tqlQuery));
    }

    @Test
    public void testParseContainsIgnoreCase() {
        final String tqlQuery = "name containsIgnoreCase 'am'";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'am'"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "false"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseContainsIgnoreCaseWithExpectedException() {
        final String tqlQuery = "name containsIgnoreCase";
        assertThrows(TqlException.class, () -> TqlToDselConverter.convertForDb(tqlQuery));
    }

    @Test
    public void testParseBetweenForString() {
        final String tqlQuery = "field1 between ['value1', 'value4']";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value1'"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value4'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseBetweenForInt() {
        final String tqlQuery = "field1 between [2, 51]";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "2"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "51"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseBetweenForLong() {
        final String tqlQuery = "field1 between [1665577759000, 1705577759000]";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.LONG_LITERAL, "1665577759000"));
        expected.addChild(new ELNode(ELNodeType.LONG_LITERAL, "1705577759000"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseBetweenForMix() {
        final String tqlQuery = "field1 between [3182, 4722.189]";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "3182"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "4722.189"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseSingleAnd() {
        final String query = "field1=123 and field2<124";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChildren(lst);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseDoubleAnd() {
        final String query = "field1=123 and field2<124 and field3>125";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChildren(lst);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseSingleOr() {
        final String query = "field1=123 or field2<124";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        expected.addChildren(lst);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseDoubleOr() {
        final String query = "field1=123 or field2<124 or field3>125";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        expected.addChildren(lst);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseAndOr() {
        final String query = "field1=123 and field2<124 or field3>125 and field4<=126";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        List<ELNode> lst1 = new ArrayList<>();
        List<ELNode> lst2 = new ArrayList<>();
        lst1.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst1.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst2.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        lst2.add(buildNode(ELNodeType.LOWER_OR_EQUAL, "<=", "field4", "126"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        ELNode andNode1 = new ELNode(ELNodeType.AND, "&&");
        ELNode andNode2 = new ELNode(ELNodeType.AND, "&&");
        andNode1.addChildren(lst1);
        andNode2.addChildren(lst2);
        expected.addChild(andNode1);
        expected.addChild(andNode2);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseLiteralComparisonEq() {
        final String query = "field1=123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.EQUAL, "==", "field1", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonNeq() {
        final String query = "field1!=123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.NOT_EQUAL, "!=", "field1", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonLt() {
        final String query = "field1<123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.LOWER_THAN, "<", "field1", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonGt() {
        final String query = "field1>123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.GREATER_THAN, ">", "field1", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonLet() {
        final String query = "field4<=123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.LOWER_OR_EQUAL, "<=", "field4", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonGet() {
        final String query = "field3>=123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.GREATER_OR_EQUAL, ">=", "field3", "123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonNegative() {
        final String query = "field2=-123";
        ELNode actual = TqlToDselConverter.convertForDb(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.EQUAL, "==", "field2", "-123");

        assertEqualsForELNodes(actual, expected, false);
    }

    @Test
    public void testParseInForInt() {
        final String query = "field1 in [89178, 12, 99, 2]";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "in");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "89178"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "12"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "99"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "2"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseInForString() {
        final String query = "field1 in ['value1', 'value2']";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "in");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value1'"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value2'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseInForDecimal() {
        final String query = "field1 in [525.87, 12.18928, 99.20, 252.0]";
        ELNode actual = TqlToDselConverter.convertForDb(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "in");
        expected.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "525.87"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "12.18928"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "99.20"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "252.0"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsValidWithTqlExpression() {
        final String tqlQuery = "name is valid";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convertForDb(tqlExpression, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isValid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsValid() {
        final String tqlQuery = "name is valid";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isValid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsValidWithExpectedException() {
        final String tqlQuery = "unknown is valid";

        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convertForDb(tqlQuery, fieldToType));
        assertEquals("Cannot find the type of the field 'unknown'", exception.getMessage());
    }

    @Test
    public void testParseIsInvalidWithTqlExpression() {
        final String tqlQuery = "name is invalid";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convertForDb(tqlExpression, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsInvalid() {
        final String tqlQuery = "name is invalid";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsInvalidWithAllFields() {
        final String tqlQuery = "* is invalid";
        ELNode actual = TqlToDselConverter.convertForDb(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "hasInvalid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "'*'"));

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsInvalidWithAllFieldsForRuntime() {
        final String tqlQuery = "* is invalid";
        ELNode actual = TqlToDselConverter.convertForRuntime(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.OR, "||");

        ELNode isInvalidNode1 = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isInvalidNode1.addChild(new ELNode(ELNodeType.STRING_LITERAL, "special"));
        isInvalidNode1.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + fieldToType.get("special") + "'"));

        expected.addChild(isInvalidNode1);

        ELNode isInvalidNode2 = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isInvalidNode2.addChild(new ELNode(ELNodeType.STRING_LITERAL, "total"));
        isInvalidNode2.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + fieldToType.get("total") + "'"));

        expected.addChild(isInvalidNode2);

        ELNode isInvalidNode3 = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isInvalidNode3.addChild(new ELNode(ELNodeType.STRING_LITERAL, "money"));
        isInvalidNode3.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + fieldToType.get("money") + "'"));
        expected.addChild(isInvalidNode3);

        ELNode isInvalidNode4 = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isInvalidNode4.addChild(new ELNode(ELNodeType.STRING_LITERAL, "name"));
        isInvalidNode4.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + fieldToType.get("name") + "'"));
        expected.addChild(isInvalidNode4);

        ELNode isInvalidNode5 = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isInvalidNode5.addChild(new ELNode(ELNodeType.STRING_LITERAL, "isActivated"));
        isInvalidNode5.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + fieldToType.get("isActivated") + "'"));
        expected.addChild(isInvalidNode5);

        assertEqualsForELNodes(actual, expected, true);
    }

    @Test
    public void testParseIsInvalidWithExpectedException() {
        final String tqlQuery = "unknown is invalid";
        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convertForDb(tqlQuery, fieldToType));
        assertEquals("Cannot find the 'type' of the field 'unknown'", exception.getMessage());
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));

        if (value != null) {
            current.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        }

        return current;
    }

    private ELNode buildNodeIncludingRootAndExprBlock(ELNodeType type, String image, String name, String value) {
        ELNode root = new ELNode(ELNodeType.ROOT);
        ELNode expBlk = new ELNode(ELNodeType.EXPR_BLOCK);
        ELNode eq = new ELNode(type, image);
        eq.addChild(new ELNode(ELNodeType.HPATH, name));
        eq.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        expBlk.addChild(eq);
        root.addChild(expBlk);

        return root;
    }

    private static void assertEqualsForELNodes(ELNode actual, ELNode expected, boolean isExpectedMustBeWrapped) {
        ELNodePrinter elNodePrinter = new ELNodePrinter("-", false);

        ELNode finalExpected = isExpectedMustBeWrapped ? wrapNode(expected) : expected;

        assertEquals(finalExpected, actual, "\nTree of expected:\n" + elNodePrinter.printAsTree(finalExpected)
                + " \nTree of actual:\n" + elNodePrinter.printAsTree(actual));
        assertEquals(expected.toString(), actual.toString(), "\nTree of expected:\n" + elNodePrinter.printAsTree(finalExpected)
                + " \nTree of actual:\n" + elNodePrinter.printAsTree(actual));
    }

}
