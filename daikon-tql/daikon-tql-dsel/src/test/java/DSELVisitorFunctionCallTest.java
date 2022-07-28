import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tqldsel.DSELConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.tqldsel.DSELConverter.wrapNode;

public class DSELVisitorFunctionCallTest {

    static Map<String, String> fieldToType;

    @BeforeAll
    static void setUp() {
        final HashMap<String, String> fToType = new HashMap<>();
        fToType.put("name", "STRING");
        fToType.put("total", "INTEGER");
        fieldToType = Collections.unmodifiableMap(fToType);
    }

    @Test
    public void testParseIsNull() {
        final String tqlQuery = "field1 is null";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);
        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", "null");

        assertEquals(wrapNode(nullNode), actual);
    }

    @Test
    public void testParseNotIsNull() {
        final String tqlQuery = "not(field1 is null)";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode notNode = new ELNode(ELNodeType.NOT, "!");
        ELNode nullNode = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field1", "null");
        notNode.addChild(nullNode);

        assertEquals(wrapNode(notNode), actual);
    }

    @Test
    public void testParseIsEmtpy() {
        final String tqlQuery = "field1 is empty";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", "empty");

        assertEquals(wrapNode(isEmptyNode), actual);
    }

    @Test
    public void testParseIsEmtpyAnd() {
        final String tqlQuery = "(field1 is empty) and ((field2 is null))";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);
        ELNode isEmptyNode = buildNode(ELNodeType.FUNCTION_CALL, "isEmpty", "field1", "empty");
        ELNode isNull = buildNode(ELNodeType.FUNCTION_CALL, "isNull", "field2", "null");
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChild(isEmptyNode);
        andNode.addChild(isNull);

        assertEquals(wrapNode(andNode), actual);
    }

    @Test
    public void testParseRegEx() {
        final String tqlQuery = "name ~ '^[A-Z][a-z]*$'";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode regexNode = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        regexNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        regexNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "^[A-Z][a-z]*$"));

        assertEquals(wrapNode(regexNode), actual);
    }

    @Test
    public void testParseRegEx2() {
        final String tqlQuery = "name ~ '\\d'";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode regexNode = new ELNode(ELNodeType.FUNCTION_CALL, "matches");
        regexNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        regexNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "\\d"));

        assertEquals(wrapNode(regexNode), actual);
    }

    @Test
    public void testParseContains() {
        final String tqlQuery = "name contains 'am'";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "am"));
        containsNode.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseNotContains() {
        final String tqlQuery = "name contains 'bla'";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "bla"));
        containsNode.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseContainsCase() {
        final String tqlQuery = "name containsIgnoreCase 'am'";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode containsNode = new ELNode(ELNodeType.FUNCTION_CALL, "contains");
        containsNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        containsNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "am"));

        assertEquals(wrapNode(containsNode), actual);
    }

    @Test
    public void testParseIsValid() {
        final String tqlQuery = "name is valid";
        ELNode actual = new DSELConverter().convert(tqlQuery, fieldToType);

        ELNode isValidNode = new ELNode(ELNodeType.FUNCTION_CALL, "isValid");
        isValidNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        isValidNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "STRING"));

        assertEquals(wrapNode(isValidNode), actual);
    }


    @Test
    public void testParseIsValidWithExpectedException() {
        final String tqlQuery = "unknown is valid";
        final DSELConverter dselConverter = new DSELConverter();

        Exception exception = assertThrows(TqlException.class, () ->
                dselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Cannot find the type of the field 'unknown'", exception.getMessage());
    }

    @Test
    public void testParseIsInvalid() {
        final String tqlQuery = "name is invalid";
        final DSELConverter dselConverter = new DSELConverter();
        ELNode actual = dselConverter.convert(tqlQuery, fieldToType);

        ELNode isValidNode = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
        isValidNode.addChild(new ELNode(ELNodeType.HPATH, "name"));
        isValidNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "STRING"));

        assertEquals(wrapNode(isValidNode), actual);
    }


    @Test
    public void testParseIsInvalidWithExpectedException() {
        final String tqlQuery = "unknown is invalid";
        final DSELConverter dselConverter = new DSELConverter();
        Exception exception = assertThrows(TqlException.class, () ->
                dselConverter.convert(tqlQuery, fieldToType));
        assertEquals("Cannot find the 'type' of the field 'unknown'", exception.getMessage());
    }

    @Test
    public void testParseContainsException() {
        final String tqlQuery = "name contains";
        assertThrows(TqlException.class, () -> new DSELConverter().convert(tqlQuery, fieldToType));
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));
        return current;
    }
}
