import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tqldsel.DSELConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.tqldsel.DSELConverter.wrapNode;

public class DSELVisitorFunctionCallTest {

    @Test
    public void testParseIsNull() {
        final String query = "field1 is null";
        ELNode actual = DSELConverter.convert(query);
        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", "null");

        assertEquals(wrapNode(nullNode), actual);
    }

    @Test
    public void testParseNotIsNull() {
        final String query = "not(field1 is null)";
        ELNode actual = DSELConverter.convert(query);

        ELNode notNode = new ELNode(ELNodeType.NOT, "!");
        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", "null");
        notNode.addChild(nullNode);

        assertEquals(wrapNode(notNode), actual);
    }

    @Test
    public void testParseIsEmtpy() {
        final String query = "field1 is empty";
        ELNode actual = DSELConverter.convert(query);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", "empty");

        assertEquals(wrapNode(isEmptyNode), actual);
    }

    @Test
    public void testParseIsEmtpyAnd() {
        final String query = "(field1 is empty) and ((field2 is null))";
        ELNode actual = DSELConverter.convert(query);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", "empty");
        ELNode isNull = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field2", "null");
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChild(isEmptyNode);
        andNode.addChild(isNull);

        assertEquals(wrapNode(andNode), actual);
    }

    @Test
    public void testParseRegEx() {
        final String query = "name ~ '^[A-Z][a-z]*$'";
        ELNode actual = DSELConverter.convert(query);

        ELNode regexNode = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        regexNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        regexNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "^[A-Z][a-z]*$"));

        assertEquals(wrapNode(regexNode), actual);
    }

    @Test
    public void testParseRegEx2() {
        final String query = "name ~ '\\d'";
        ELNode actual = DSELConverter.convert(query);

        ELNode regexNode = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        regexNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        regexNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "\\d"));

        assertEquals(wrapNode(regexNode), actual);
    }

    @Test
    public void testParseContains() {
        final String query = "name contains 'am'";
        ELNode actual = DSELConverter.convert(query);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "am"));
        containsNode.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseNotContains() {
        final String query = "name contains 'bla'";
        ELNode actual = DSELConverter.convert(query);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "bla"));
        containsNode.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseContainsCase() {
        final String query = "name containsIgnoreCase 'am'";
        ELNode actual = DSELConverter.convert(query);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "am"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseContainsException() {
        final String query = "name contains";
        assertThrows(TqlException.class, () -> DSELConverter.convert(query));
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));
        return current;
    }
}
