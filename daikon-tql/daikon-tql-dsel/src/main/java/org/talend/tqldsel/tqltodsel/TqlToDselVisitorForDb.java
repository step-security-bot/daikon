package org.talend.tqldsel.tqltodsel;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.AllFields;
import org.talend.tql.model.FieldIsEmptyExpression;
import org.talend.tql.model.FieldIsInvalidExpression;
import org.talend.tql.model.TqlElement;
import org.talend.tql.visitor.IASTVisitor;

/**
 * TQL to DSEL visitor used to store in database
 */
public class TqlToDselVisitorForDb extends AbstractTqlToDselVisitor implements IASTVisitor<ELNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TqlToDselVisitorForDb.class);

    /**
     *
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a lightweight
     * representation of the schema
     */
    public TqlToDselVisitorForDb(Map<String, String> fieldToType) {
        super(fieldToType);
    }

    public ELNode visit(FieldIsEmptyExpression elt) {
        LOGGER.debug("Inside Visit FieldIsEmptyExpression " + elt.toString());
        final TqlElement ex = elt.getField();

        ELNode isEmptyNode;

        if (ex instanceof AllFields) {
            isEmptyNode = new ELNode(ELNodeType.FUNCTION_CALL, "hasEmpty");
            isEmptyNode.addChild(new ELNode(ELNodeType.HPATH, "'*'"));
        } else {
            isEmptyNode = new ELNode(ELNodeType.FUNCTION_CALL,
                    org.talend.maplang.el.interpreter.impl.function.builtin.IsEmpty.NAME);
            isEmptyNode.addChild(ex.accept(this));
        }

        return isEmptyNode;
    }

    public ELNode visit(FieldIsInvalidExpression elt) {
        LOGGER.debug("Inside Visit FieldIsInvalidExpression " + elt.toString());
        final TqlElement ex = elt.getField();

        ELNode isInvalidNode;

        if (ex instanceof AllFields) {
            isInvalidNode = new ELNode(ELNodeType.FUNCTION_CALL, "hasInvalid");
            isInvalidNode.addChild(new ELNode(ELNodeType.HPATH, "'*'"));
        } else {
            final ELNode node = ex.accept(this);

            isInvalidNode = new ELNode(ELNodeType.FUNCTION_CALL, "isInvalid");
            isInvalidNode.addChild(node);

            final String invalidFieldType = fieldToType.get(node.getImage());
            if (invalidFieldType == null) {
                throw new TqlException(String.format("Cannot find the 'type' of the field '%s'", node.getImage()));
            }

            isInvalidNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, invalidFieldType));
        }

        return isInvalidNode;
    }
}
