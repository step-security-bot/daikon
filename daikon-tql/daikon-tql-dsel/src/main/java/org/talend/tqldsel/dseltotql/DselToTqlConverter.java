package org.talend.tqldsel.dseltotql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.ExprParser;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodePrinter;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.TqlElement;

public class DselToTqlConverter {

    private static final DselToTqlVisitor visitor = new DselToTqlVisitor();

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param dselQuery TQL query as String
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static TqlElement convert(final String dselQuery) throws TqlException {
        return convert(new ExprParser().parse(dselQuery));
    }

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param dselQuery TQL query
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static TqlElement convert(final ELNode dselQuery) throws TqlException {
        return dselQuery.accept(visitor);
    }

    /**
     * Utility class to print the ELNode as a tree
     *
     * @param elNode ELNode to print
     */
    public static void printELNode(ELNode elNode) {
        Logger LOGGER = LoggerFactory.getLogger(DselToTqlConverter.class);

        final String TEST_TAB = "-";
        LOGGER.debug(new ELNodePrinter(TEST_TAB, false).printAsTree(elNode));
    }
}
