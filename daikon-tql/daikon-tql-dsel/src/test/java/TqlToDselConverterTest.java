import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.talend.maplang.el.interpreter.api.DselHPathStore;
import org.talend.maplang.el.interpreter.api.ExprInterpreter;
import org.talend.maplang.el.interpreter.api.ExprInterpreterFactory;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.maplang.hpath.HPathStore;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;
import org.talend.tql.parser.Tql;
import org.talend.tqldsel.tqltodsel.TqlToDselConverter;

public class TqlToDselConverterTest {

    static Map<String, String> fieldToType;

    static HPathStore store = new DselHPathStore();

    @BeforeAll
    static void setUp() {
        final HashMap<String, String> fToType = new HashMap<>();
        fToType.put("name", "STRING");
        fToType.put("total", "INTEGER");
        fToType.put("isActivated", "BOOLEAN");
        fToType.put("money", "DECIMAL");
        fToType.put("special", "semanticType1");
        fieldToType = Collections.unmodifiableMap(fToType);

        store.put("fieldLong1", "1680000000000L");
        store.put("fieldInt1", "123");
        store.put("fieldInt2", "124");
        store.put("fieldInt3", "499");
        store.put("fieldInt4", "978");
        store.put("fieldDec1", "3500.057");
        store.put("fieldString1", "value2");
        store.put("fieldString2", "valueOther");
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));

        if (value != null) {
            current.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        }

        return current;
    }

    private ELNode buildNodeIncludingRootAndExprBlock(final ELNodeType type, final String image, final String name,
            final String value) {
        ELNode root = new ELNode(ELNodeType.ROOT);
        ELNode expBlk = new ELNode(ELNodeType.EXPR_BLOCK);
        ELNode eq = new ELNode(type, image);
        eq.addChild(new ELNode(ELNodeType.HPATH, name));
        eq.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        expBlk.addChild(eq);
        root.addChild(expBlk);

        return root;
    }

    @Test
    public void testParseSingleNot() {
        final String query = "not (fieldInt1=123 and fieldInt2<124)";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChildren(lst);
        ELNode expected = new ELNode(ELNodeType.NOT, "!");
        expected.addChild(andNode);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsNullWithTqlExpression() {
        final String tqlQuery = "fieldInt1 is null";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convert(tqlExpression);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "fieldInt1", null);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsNull() {
        final String tqlQuery = "fieldInt1 is null";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "fieldInt1", null);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseNotIsNull() {
        final String tqlQuery = "not(fieldInt1 is null)";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "fieldInt1", null);
        ELNode expected = new ELNode(ELNodeType.NOT, "!");
        expected.addChild(nullNode);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsEmpty() {
        final String tqlQuery = "fieldString1 is empty";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);
        ELNode expected = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "fieldString1", null);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsEmptyAnd() {
        final String tqlQuery = "(fieldString1 is empty) and ((fieldString2 is null))";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "fieldString1", null);
        ELNode isNull = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "fieldString2", null);
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChild(isEmptyNode);
        expected.addChild(isNull);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsEmptyWithAllFieldsWithExpectedException() {
        final String tqlQuery = "* is empty";
        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Unsupported operation : visit(AllFields elt)", exception.getMessage());
    }

    @Test
    public void testParseTqlLiteralWithSingleQuote() {
        final String tqlQuery = "name = 'abc\\'def'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.EQUAL);
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'abc\\'def'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseRegEx() {
        final String tqlQuery = "fieldString1 ~ '^[A-Z][a-z]*$'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'^[A-Z][a-z]*$'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseRegEx2() {
        final String tqlQuery = "fieldString1 ~ '\\d'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'\\d'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseRegExWithSingleQuote() {
        final String tqlQuery = "fieldString1 ~ '\\d\\'\\w'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'\\d\\'\\w'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseTqlWordComplies() {
        final String tqlQuery = "name wordComplies '[word]'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "wordComplies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'[word]'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseTqlWordCompliesWithSingleQuote() {
        final String tqlQuery = "name wordComplies '[word]\\'[word]'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "wordComplies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'[word]\\'[word]'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseTqlComplies() {
        final String tqlQuery = "name complies 'aaa'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "complies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'aaa'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseTqlCompliesWithSingleQuote() {
        final String tqlQuery = "name complies 'aaa\\'aaa'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "complies");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'aaa\\'aaa'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseContains() {
        final String tqlQuery = "fieldString1 contains 'am'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'am'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseContainsWithSingleQuote() {
        final String tqlQuery = "fieldString1 contains 'I\\'m'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'I\\'m'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseContainsWithExpectedException() {
        final String tqlQuery = "fieldString1 contains";
        assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery));
    }

    @Test
    public void testParseContainsIgnoreCase() {
        final String tqlQuery = "fieldString1 containsIgnoreCase 'am'";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'am'"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseContainsIgnoreCaseWithExpectedException() {
        final String tqlQuery = "name containsIgnoreCase";
        assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery));
    }

    @Test
    public void testParseBetweenForString() {
        final String tqlQuery = "fieldString1 between ]'value1', 'value4']";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value1'"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value4'"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseBetweenForInt() {
        final String tqlQuery = "fieldInt1 between ]2, 187[";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldInt1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "2"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "187"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseBetweenForLong() {
        final String tqlQuery = "fieldLong1 between [1665577759000, 1705577759000[";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldLong1"));
        expected.addChild(new ELNode(ELNodeType.LONG_LITERAL, "1665577759000L"));
        expected.addChild(new ELNode(ELNodeType.LONG_LITERAL, "1705577759000L"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "false"));
        expected.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseBetweenForMix() {
        final String tqlQuery = "fieldDec1 between [3182, 4722.189]";
        ELNode actual = TqlToDselConverter.convert(tqlQuery);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "between");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldDec1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "3182"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "4722.189"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseSingleAnd() {
        final String query = "fieldInt1=123 and fieldInt2<124";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChildren(lst);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseDoubleAnd() {
        final String query = "fieldInt1=123 and fieldInt2<124 and fieldInt3>125";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "fieldInt3", "125"));
        ELNode expected = new ELNode(ELNodeType.AND, "&&");
        expected.addChildren(lst);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseSingleOr() {
        final String query = "fieldInt1=123 or fieldInt2<124";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        expected.addChildren(lst);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseDoubleOr() {
        final String query = "fieldInt1=123 or fieldInt2<124 or fieldInt3>125";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst = new ArrayList<>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "fieldInt3", "125"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        expected.addChildren(lst);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseAndOr() {
        final String query = "fieldInt1=123 and fieldInt2<124 or fieldInt3>125 and fieldInt4<=126";
        ELNode actual = TqlToDselConverter.convert(query);

        List<ELNode> lst1 = new ArrayList<>();
        List<ELNode> lst2 = new ArrayList<>();
        lst1.add(buildNode(ELNodeType.EQUAL, "==", "fieldInt1", "123"));
        lst1.add(buildNode(ELNodeType.LOWER_THAN, "<", "fieldInt2", "124"));
        lst2.add(buildNode(ELNodeType.GREATER_THAN, ">", "fieldInt3", "125"));
        lst2.add(buildNode(ELNodeType.LOWER_OR_EQUAL, "<=", "fieldInt4", "126"));
        ELNode expected = new ELNode(ELNodeType.OR, "||");
        ELNode andNode1 = new ELNode(ELNodeType.AND, "&&");
        ELNode andNode2 = new ELNode(ELNodeType.AND, "&&");
        andNode1.addChildren(lst1);
        andNode2.addChildren(lst2);
        expected.addChild(andNode1);
        expected.addChild(andNode2);

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseLiteralComparisonEq() {
        final String query = "fieldInt1=123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.EQUAL, "==", "fieldInt1", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonNeq() {
        final String query = "fieldInt1!=123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.NOT_EQUAL, "!=", "fieldInt1", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonLt() {
        final String query = "fieldInt1<123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.LOWER_THAN, "<", "fieldInt1", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonGt() {
        final String query = "fieldInt1>123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.GREATER_THAN, ">", "fieldInt1", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonLet() {
        final String query = "fieldInt4<=123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.LOWER_OR_EQUAL, "<=", "fieldInt4", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonGet() {
        final String query = "fieldInt3>=123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.GREATER_OR_EQUAL, ">=", "fieldInt3", "123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseLiteralComparisonNegative() {
        final String query = "fieldInt2=-123";
        ELNode actual = TqlToDselConverter.convert(query);
        ELNode expected = buildNodeIncludingRootAndExprBlock(ELNodeType.EQUAL, "==", "fieldInt2", "-123");

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, false);
    }

    @Test
    public void testParseInForInt() {
        final String query = "fieldInt1 in [89178, 12, 123, 9]";
        ELNode actual = TqlToDselConverter.convert(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isPresentIn");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldInt1"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "89178"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "12"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "123"));
        expected.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, "9"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseInForString() {
        final String query = "fieldString1 in ['value1', 'value2']";
        ELNode actual = TqlToDselConverter.convert(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isPresentIn");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldString1"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value1'"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'value2'"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseInForDecimal() {
        final String query = "fieldDec1 in [525.87, 3500.057, 99.20, 252.0]";
        ELNode actual = TqlToDselConverter.convert(query);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isPresentIn");
        expected.addChild(new ELNode(ELNodeType.HPATH, "fieldDec1"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "525.87"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "3500.057"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "99.20"));
        expected.addChild(new ELNode(ELNodeType.DECIMAL_LITERAL, "252.0"));

        assertELNodesAreEqualsAndExecutionIsOK(actual, expected, true);
    }

    @Test
    public void testParseIsValidWithTqlExpression() {
        final String tqlQuery = "name is valid";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convert(tqlExpression, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isValid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseIsValid() {
        final String tqlQuery = "name is valid";
        ELNode actual = TqlToDselConverter.convert(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isValid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseIsValidWithExpectedException() {
        final String tqlQuery = "unknown is valid";

        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Cannot find the type of the field 'unknown'", exception.getMessage());
    }

    @Test
    public void testParseIsInvalidWithTqlExpression() {
        final String tqlQuery = "name is invalid";
        final Expression tqlExpression = Tql.parse(tqlQuery);
        ELNode actual = TqlToDselConverter.convert(tqlExpression, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseIsInvalid() {
        final String tqlQuery = "name is invalid";
        ELNode actual = TqlToDselConverter.convert(tqlQuery, fieldToType);

        ELNode expected = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        expected.addChild(new ELNode(ELNodeType.HPATH, "name"));
        expected.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'STRING'"));

        assertELNodesAreEquals(actual, expected, true, false);
    }

    @Test
    public void testParseIsInvalidWithExpectedException() {
        final String tqlQuery = "unknown is invalid";
        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Cannot find the 'type' of the field 'unknown'", exception.getMessage());
    }

    @Test
    public void testParseIsInvalidWithAllFieldsWithExpectedException() {
        final String tqlQuery = "* is invalid";
        Exception exception = assertThrows(TqlException.class, () -> TqlToDselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Unsupported operation : visit(AllFields elt)", exception.getMessage());
    }

    private static void assertELNodesAreEqualsAndExecutionIsOK(final ELNode actual, final ELNode expected,
            final boolean isExpectedMustBeWrapped) {
        assertELNodesAreEquals(actual, expected, isExpectedMustBeWrapped, true);
    }

    private static void assertELNodesAreEquals(final ELNode actual, final ELNode expected, final boolean isExpectedMustBeWrapped,
            final boolean isExecutionChecked) {
        ELNodePrinter elNodePrinter = new ELNodePrinter("-", false);

        ELNode finalExpected = isExpectedMustBeWrapped ? wrapNode(expected) : expected;

        assertEquals(finalExpected, actual, "\nTree of expected:\n" + elNodePrinter.printAsTree(finalExpected)
                + " \nTree of actual:\n" + elNodePrinter.printAsTree(actual));
        assertEquals(expected.toString(), actual.toString(), "\nTree of expected:\n" + elNodePrinter.printAsTree(finalExpected)
                + " \nTree of actual:\n" + elNodePrinter.printAsTree(actual));

        if (isExecutionChecked) {
            ExprLangContext context = new ExprLangContext();
            context.setStore(store);
            ExprInterpreter interpreter = ExprInterpreterFactory.create(context);
            interpreter.setModel(actual);
            assertDoesNotThrow(interpreter::eval, "DSEL expression should not throw an exception because it should be valid");
        }
    }

}
