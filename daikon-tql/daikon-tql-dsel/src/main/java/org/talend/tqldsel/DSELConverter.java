package org.talend.tqldsel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;
import org.talend.tql.parser.Tql;

import java.util.HashMap;
import java.util.Map;

public class DSELConverter {

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param query TQL query as String
     * @return DSEL ELNode ready to serve for DSEL intepreter
     */
    public ELNode convert(String query, Map<String, String> fieldToType) throws TqlException {
        DSELVisitor visitor = new DSELVisitor(fieldToType);
        Expression exp = Tql.parse(query);
        ELNode raw = exp.accept(visitor);

        return wrapNode(raw);
    }

    /**
     * Wraps an ELNode inside an ELNodeType.ROOT node
     * (mendatory for interpreting)
     * 
     * @param node ELNode to be wrapped
     * @return ELNode wrapped
     */
    public static ELNode wrapNode(ELNode node) {
        ELNode root = new ELNode(ELNodeType.ROOT);
        ELNode expBlk = new ELNode(ELNodeType.EXPR_BLOCK);
        expBlk.addChild(node);
        root.addChild(expBlk);

        return root;
    }

    /**
     * Utility class to print the ELNode as a tree
     * 
     * @param tree
     */
    public static void printELNode(ELNode tree) {
        Logger LOGGER = LoggerFactory.getLogger(DSELVisitor.class);

        final String TEST_TAB = "-";

        StringBuilder buf = new StringBuilder();
        tree.exportAsReadableString(buf, TEST_TAB, false);
        LOGGER.debug(buf.toString());
    }
}
