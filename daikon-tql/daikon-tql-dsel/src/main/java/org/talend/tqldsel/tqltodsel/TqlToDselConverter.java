package org.talend.tqldsel.tqltodsel;

import java.util.Collections;
import java.util.Map;

import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;
import org.talend.tql.parser.Tql;

public class TqlToDselConverter {

    /**
     * Utility method to convert a TQL query as String to a DSEL query.
     *
     * @param tqlQuery TQL tqlQuery as String
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a lightweight
     * representation of the schema. <b>Required</b> for expressions containing: <code>isValid(...)</code>,
     * <code>isInvalid(...)</code>
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final String tqlQuery, Map<String, String> fieldToType) throws TqlException {
        final Expression tqlExpression = Tql.parse(tqlQuery);
        return convert(tqlExpression, fieldToType);
    }

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param tqlQuery TQL query
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a lightweight
     * representation of the schema
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final Expression tqlQuery, Map<String, String> fieldToType) throws TqlException {
        TqlToDselVisitor visitor = new TqlToDselVisitor(fieldToType);
        final ELNode raw = tqlQuery.accept(visitor);
        return wrapNode(raw);
    }

    /**
     * Utility method to convert a TQL query as String to a DSEL query.
     *
     * @param tqlQuery TQL query as String
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final String tqlQuery) throws TqlException {
        return convert(tqlQuery, Collections.emptyMap());
    }

    /**
     * Utility method to convert a TQL query to a DSEL query.
     *
     * @param tqlQuery TQL query
     * @return DSEL ELNode ready to serve for DSEL interpreter
     */
    public static ELNode convert(final Expression tqlQuery) throws TqlException {
        return convert(tqlQuery, Collections.emptyMap());
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
}
