package org.talend.tqldsel.tqltodsel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.AllFields;
import org.talend.tql.model.FieldContainsExpression;
import org.talend.tql.model.ComparisonExpression;
import org.talend.tql.model.ComparisonOperator;
import org.talend.tql.model.FieldIsEmptyExpression;
import org.talend.tql.model.FieldIsInvalidExpression;
import org.talend.tql.model.TqlElement;
import org.talend.tql.visitor.IASTVisitor;

import java.util.Map;
import static org.talend.tql.model.ComparisonOperator.Enum.GET;
import static org.talend.tql.model.ComparisonOperator.Enum.GT;
import static org.talend.tql.model.ComparisonOperator.Enum.LET;
import static org.talend.tql.model.ComparisonOperator.Enum.LT;

/**
 * TQL to DSEL visitor used for runtime
 */
public class TqlToDselVisitorForRuntime extends AbstractTqlToDselVisitor implements IASTVisitor<ELNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TqlToDselVisitorForRuntime.class);

    /**
     * REGEX used to identify a number. Below the rules:
     * - spaces are allowed at the beginning and at the end
     * - we can have "+" or "-" or "(" or "(+" or "(-" at the beginning
     * - we can have ")" or "%" or "%)" at the end
     * - "." and "," are allowed
     * - we cannot have "x."
     * - we can have ".x"
     * - we can have "e" or "E"
     * - everything behind the "e" follow the previous rules
     * - we cannot have "e-" but we can have "e-5"
     *
     */
    public static final String NUMBER_REGEX_STRING_FORMAT = "\"[ ]*\\(?[-\\+]?(?:(?:(?:\\d*[\\.,]?\\d+)?[eE](:?[-\\+]\\d+)?(:?\\d*[\\.,]?\\d+)?)|(?:\\d*[\\.,]?\\d+))%?\\)?[ ]*\"";

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

            elNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, "'" + invalidFieldType + "'"));
        }

        return elNode;
    }

    @Override
    public ELNode visit(FieldContainsExpression elt) {
        ELNode containsNode = super.visit(elt);
        // for runtime we consider the contains function as containsIgnoreCase
        // the condition below was added to avoid adding a new child with true attribute if already exists
        // contains(field, 'value') -> 2 children (attribute 'true' does not exists)
        // contains(field, 'value', true) -> 3 children (attribute 'true' already exists)
        if (containsNode.getChildren().size() == 2) {
            containsNode.addChild(new ELNode(ELNodeType.BOOLEAN_LITERAL, "true"));
        }
        return containsNode;
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

    /**
     * We want to override all comparison operators: <, >, <=, >=
     * Instead of having a filter A>B, we want to have isNumber(A) && A>B
     *
     * @param elt element to visit
     * @return ELNode
     */
    @Override
    public ELNode visit(ComparisonExpression elt) {
        ELNode opNode = super.visit(elt);
        ELNode fieldNode = opNode.getChildren().get(0);
        final ComparisonOperator.Enum comparisonOperator = elt.getOperator().getOperator();
        if (GT.equals(comparisonOperator) || LT.equals(comparisonOperator) || GET.equals(comparisonOperator)
                || LET.equals(comparisonOperator)) {
            ELNode matchNumberNode = new ELNode(ELNodeType.FUNCTION_CALL,
                    org.talend.maplang.el.interpreter.impl.function.builtin.Matches.NAME);
            matchNumberNode.addChild(new ELNode(ELNodeType.HPATH, fieldNode.getImage()));
            matchNumberNode.addChild(new ELNode(ELNodeType.STRING_LITERAL, NUMBER_REGEX_STRING_FORMAT));
            final ELNode andNode = new ELNode(ELNodeType.AND);
            andNode.addChild(matchNumberNode);
            andNode.addChild(opNode);
            return andNode;
        } else {
            return opNode;
        }
    }
}
