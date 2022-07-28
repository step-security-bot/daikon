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
     * Utility method to convert a TQL tqlQuery to a DSEL tqlQuery.
     *
     * @param tqlQuery TQL tqlQuery as String
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a lightweight representation of the schema
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public ELNode convert(final String tqlQuery, Map<String, String> fieldToType) throws TqlException {
        final Expression tqlExpression = Tql.parse(tqlQuery);
        return convert(tqlExpression, fieldToType);
    }

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param tqlQuery TQL query
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a lightweight representation of the schema
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final Expression tqlQuery, Map<String, String> fieldToType) throws TqlException {
        DSELVisitor visitor = new DSELVisitor(fieldToType);
        final ELNode raw = tqlQuery.accept(visitor);
        return wrapNode(raw);
    }

    /**
     * Wraps an ELNode inside an {@link ELNodeType#ROOT} node
     * (mandatory for interpreting)
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
     * @param tree ELNode to print
     */
    public static void printELNode(ELNode tree) {
        Logger LOGGER = LoggerFactory.getLogger(DSELVisitor.class);

        final String TEST_TAB = "-";

        StringBuilder buf = new StringBuilder();
        tree.exportAsReadableString(buf, TEST_TAB, false);
        LOGGER.debug(buf.toString());
    }
}
