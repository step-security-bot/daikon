package org.talend.tqldsel.tqltodsel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;
import org.talend.tql.parser.Tql;

public class TqlToDselConverter {

    private static final TqlToDselVisitor visitor = new TqlToDselVisitor();

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param tqlQuery TQL query as String
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final String tqlQuery) throws TqlException {
        final Expression tqlExpression = Tql.parse(tqlQuery);
        return convert(tqlExpression);
    }

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param tqlQuery TQL query
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final Expression tqlQuery) throws TqlException {
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
     * @param elNode ELNode to print
     */
    public static void printELNode(ELNode elNode) {
        Logger LOGGER = LoggerFactory.getLogger(TqlToDselConverter.class);

        final String TEST_TAB = "-";
        LOGGER.debug(new ELNodePrinter(TEST_TAB, false).printAsTree(elNode));
    }
}
