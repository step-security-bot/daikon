import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tqldsel.DSELConverter;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DSELVisitorOrAndTest {

    static Map<String, String> fieldToType;

    @BeforeAll
    static void setUp() {
        final HashMap<String, String> fToType = new HashMap<>();
        fToType.put("name", "STRING");
        fToType.put("total", "INTEGER");
        fieldToType = Collections.unmodifiableMap(fToType);
    }

    @Test
    public void testParseSingleAnd() {
        final String query = "field1=123 and field2<124";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        List<ELNode> lst = new ArrayList<ELNode>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChildren(lst);

        assertEquals(DSELConverter.wrapNode(andNode), actual);
    }

    @Test
    public void testParseDoubleAnd() {
        final String query = "field1=123 and field2<124 and field3>125";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        List<ELNode> lst = new ArrayList<ELNode>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        ELNode andNode = new ELNode(ELNodeType.AND, "&&");
        andNode.addChildren(lst);

        assertEquals(DSELConverter.wrapNode(andNode), actual);
    }

    @Test
    public void testParseSingleOr() {
        final String query = "field1=123 or field2<124";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        List<ELNode> lst = new ArrayList<ELNode>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        ELNode orNode = new ELNode(ELNodeType.OR, "||");
        orNode.addChildren(lst);

        assertEquals(DSELConverter.wrapNode(orNode), actual);
    }

    @Test
    public void testParseDoubleOr() {
        final String query = "field1=123 or field2<124 or field3>125";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        List<ELNode> lst = new ArrayList<ELNode>();
        lst.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        ELNode orNode = new ELNode(ELNodeType.OR, "||");
        orNode.addChildren(lst);

        assertEquals(DSELConverter.wrapNode(orNode), actual);
    }

    @Test
    public void testParseAndOr() {
        final String query = "field1=123 and field2<124 or field3>125 and field4<=126";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        List<ELNode> lst1 = new ArrayList<ELNode>();
        List<ELNode> lst2 = new ArrayList<ELNode>();
        lst1.add(buildNode(ELNodeType.EQUAL, "==", "field1", "123"));
        lst1.add(buildNode(ELNodeType.LOWER_THAN, "<", "field2", "124"));
        lst2.add(buildNode(ELNodeType.GREATER_THAN, ">", "field3", "125"));
        lst2.add(buildNode(ELNodeType.LOWER_OR_EQUAL, "<=", "field4", "126"));
        ELNode orNode = new ELNode(ELNodeType.OR, "||");
        ELNode andNode1 = new ELNode(ELNodeType.AND, "&&");
        ELNode andNode2 = new ELNode(ELNodeType.AND, "&&");
        andNode1.addChildren(lst1);
        andNode2.addChildren(lst2);
        orNode.addChild(andNode1);
        orNode.addChild(andNode2);

        assertEquals(DSELConverter.wrapNode(orNode), actual);
    }

    private ELNode buildNode(ELNodeType type, String image, String name, String value) {
        ELNode current = new ELNode(type, image);
        current.addChild(new ELNode(ELNodeType.HPATH, name));
        current.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        return current;
    }
}
