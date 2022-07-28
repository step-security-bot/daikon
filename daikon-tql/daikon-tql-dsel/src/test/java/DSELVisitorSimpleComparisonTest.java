import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tqldsel.DSELConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DSELVisitorSimpleComparisonTest {


    static Map<String, String> fieldToType;

    @BeforeAll
    static void setUp() {
        final HashMap<String, String> ftot = new HashMap<>();
        ftot.put("name", "STRING");
        ftot.put("total", "INTEGER");
        fieldToType = Collections.unmodifiableMap(ftot);
    }

    @Test
    public void testParseLiteralComparisonEq() {
        final String query = "field1=123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.EQUAL, "==", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonNeq() {
        final String query = "field1!=123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.NOT_EQUAL, "!=", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonLt() {
        final String query = "field1<123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.LOWER_THAN, "<", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonGt() {
        final String query = "field1>123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.GREATER_THAN, ">", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonLet() {
        final String query = "field1<=123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.LOWER_OR_EQUAL, "<=", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonGet() {
        final String query = "field1>=123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.GREATER_OR_EQUAL, ">=", "123"), actual);
    }

    @Test
    public void testParseLiteralComparisonNegative() {
        final String query = "field1=-123";
        ELNode actual = new DSELConverter().convert(query, fieldToType);

        assertEquals(buildNode(ELNodeType.EQUAL, "==", "-123"), actual);
    }

    private ELNode buildNode(ELNodeType type, String image, String value) {
        ELNode root = new ELNode(ELNodeType.ROOT);
        ELNode expBlk = new ELNode(ELNodeType.EXPR_BLOCK);
        ELNode eq = new ELNode(type, image);
        eq.addChild(new ELNode(ELNodeType.HPATH, "field1"));
        eq.addChild(new ELNode(ELNodeType.INTEGER_LITERAL, value));
        expBlk.addChild(eq);
        root.addChild(expBlk);

        return root;
    }
}
