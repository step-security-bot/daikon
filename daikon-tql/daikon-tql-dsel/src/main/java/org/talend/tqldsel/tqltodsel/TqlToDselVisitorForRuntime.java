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
 * TQL to DSEL visitor used for runtime
 */
public class TqlToDselVisitorForRuntime extends AbstractTqlToDselVisitor implements IASTVisitor<ELNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TqlToDselVisitorForRuntime.class);

    /**
     *
     * @param fieldToType a Map object used to get a type (native or semantic type) from a field name, this is a
     * lightweight
     * representation of the schema
     */
    public TqlToDselVisitorForRuntime(Map<String, String> fieldToType) {
        super(fieldToType);
    }

    public ELNode visit(FieldIsEmptyExpression elt) {
        LOGGER.debug("Inside Visit FieldIsEmptyExpression " + elt.toString());
        final TqlElement ex = elt.getField();

        ELNode elNode;

        if (ex instanceof AllFields) {
            elNode = buildOrExpressionForFunctionWithAllFields("isEmpty", false);
        } else {
            elNode = new ELNode(ELNodeType.FUNCTION_CALL, org.talend.maplang.el.interpreter.impl.function.builtin.IsEmpty.NAME);
            elNode.addChild(ex.accept(this));
        }

        return elNode;
    }

    public ELNode visit(FieldIsInvalidExpression elt) {
        LOGGER.debug("Inside Visit FieldIsInvalidExpression " + elt.toString());
        final String isInvalidFunctionName = "isInvalid";

        final TqlElement ex = elt.getField();

        ELNode elNode;

        if (ex instanceof AllFields) {
            elNode = buildOrExpressionForFunctionWithAllFields(isInvalidFunctionName, true);
        } else {
            final ELNode node = ex.accept(this);

            elNode = new ELNode(ELNodeType.FUNCTION_CALL, isInvalidFunctionName);
            elNode.addChild(node);

            final String invalidFieldType = fieldToType.get(node.getImage());
            if (invalidFieldType == null) {
                throw new TqlException(String.format("Cannot find the 'type' of the field '%s'", node.getImage()));
            }

            elNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, invalidFieldType));
        }

        return elNode;
    }

    private ELNode buildOrExpressionForFunctionWithAllFields(final String functionName, final Boolean isTypeProvided) {
        ELNode orNode = new ELNode(ELNodeType.OR, "||");

        for (Map.Entry<String, String> field : fieldToType.entrySet()) {
            ELNode functionNode = new ELNode(ELNodeType.FUNCTION_CALL, functionName);
            functionNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, field.getKey()));
            if (isTypeProvided) {
                functionNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + field.getValue() + "'"));
            }
            orNode.addChild(functionNode);
        }

        return orNode;
    }
}
