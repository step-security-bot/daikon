package org.talend.tqldsel.dseltotql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.talend.maplang.el.interpreter.impl.function.builtin.Between;
import org.talend.maplang.el.interpreter.impl.function.builtin.Contains;
import org.talend.maplang.el.interpreter.impl.function.builtin.IsEmpty;
import org.talend.maplang.el.interpreter.impl.function.builtin.IsNull;
import org.talend.maplang.el.interpreter.impl.function.builtin.Matches;
import org.talend.maplang.el.parser.DSELConstants;
import org.talend.maplang.el.parser.DslContent;
import org.talend.maplang.el.parser.model.ELNode;
import org.talend.maplang.el.parser.model.ELNodeType;
import org.talend.maplang.el.parser.model.ExprModelVisitor;
import org.talend.tql.api.TqlBuilder;
import org.talend.tql.model.AllFields;
import org.talend.tql.model.AndExpression;
import org.talend.tql.model.ComparisonExpression;
import org.talend.tql.model.ComparisonOperator;
import org.talend.tql.model.Expression;
import org.talend.tql.model.FieldBetweenExpression;
import org.talend.tql.model.FieldCompliesPattern;
import org.talend.tql.model.FieldInExpression;
import org.talend.tql.model.FieldIsEmptyExpression;
import org.talend.tql.model.FieldIsInvalidExpression;
import org.talend.tql.model.FieldIsValidExpression;
import org.talend.tql.model.FieldReference;
import org.talend.tql.model.FieldWordCompliesPattern;
import org.talend.tql.model.LiteralValue;
import org.talend.tql.model.OrExpression;
import org.talend.tql.model.TqlElement;

public class DselToTqlVisitor implements ExprModelVisitor<TqlElement> {

    private final static String BUILTIN_FUNCTION_IN = "in";

    private final static String BUILTIN_FUNCTION_IS_VALID = "isValid";

    private final static String BUILTIN_FUNCTION_IS_INVALID = "isInvalid";

    private final static String FAKE_FUNCTION_HAS_INVALID = "hasInvalid";

    private final static String FAKE_FUNCTION_HAS_EMPTY = "hasEmpty";

    private final static String BUILTIN_FUNCTION_COMPLIES = "complies";

    private final static String BUILTIN_FUNCTION_WORD_COMPLIES = "wordComplies";

    @Override
    public void setDslContent(DslContent dslContent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TqlElement visitRoot(ELNode rootNode) {
        return this.visitChildren(rootNode);
    }

    protected TqlElement visitChildren(ELNode node) {
        TqlElement value = null;
        for (int i = 0; i < node.getChildren().size(); i++) {
            ELNode child = node.getChildren().get(i);

            if (i == 0) {
                value = child.accept(this);
            } else {
                child.accept(this);
            }
        }
        return value;
    }

    @Override
    public TqlElement visitBlock(ELNode elNode) {
        return visitChildren(elNode);
    }

    @Override
    public TqlElement visitLiteral(ELNode elNode) {
        switch (elNode.getType()) {
        case STRING_LITERAL:
            return new LiteralValue(LiteralValue.Enum.QUOTED_VALUE,
                    elNode.getImage().substring(1, elNode.getImage().length() - 1));
        case INTEGER_LITERAL:
            return new LiteralValue(LiteralValue.Enum.INT, String.valueOf(elNode.getImage()));
        case DECIMAL_LITERAL:
            return new LiteralValue(LiteralValue.Enum.DECIMAL, String.valueOf(elNode.getImage()));
        case DOUBLE_LITERAL:
            return new LiteralValue(LiteralValue.Enum.DECIMAL, String.valueOf(Double.valueOf(elNode.getImage())));
        case BOOLEAN_LITERAL:
            return new LiteralValue(LiteralValue.Enum.BOOLEAN, String.valueOf(elNode.getImage()));
        default:
            // Not currently supported : LONG_LITERAL, FLOAT_LITERAL, BYTES_LITERAL, NULL_LITERAL...
            throw new IllegalStateException("Unsupported literal type : " + elNode.getType());
        }
    }

    @Override
    public TqlElement visitArray(ELNode elNode) {
        throw new IllegalStateException("Unsupported array: " + elNode.getImage());
    }

    @Override
    public TqlElement visitCompOp(ELNode elNode) {
        final ELNode leftParameter = elNode.getChild(0);
        final ELNode rightParameter = elNode.getChild(1);

        if (isLiteral(leftParameter.getType().name()) && isLiteral(rightParameter.getType().name())) {
            throw new IllegalArgumentException("Comparing two literals returns the same result for all items");
        }

        final TqlElement leftTqlElement = isLiteral(leftParameter.getType().name()) ? visitLiteral(leftParameter)
                : visitHPath(leftParameter);
        final TqlElement rightTqlElement = isLiteral(rightParameter.getType().name()) ? visitLiteral(rightParameter)
                : visitHPath(rightParameter);

        final ComparisonOperator.Enum comparisonOpEnum;

        switch (elNode.getType()) {
        case EQUAL:
            comparisonOpEnum = ComparisonOperator.Enum.EQ;
            break;
        case NOT_EQUAL:
            comparisonOpEnum = ComparisonOperator.Enum.NEQ;
            break;
        case GREATER_OR_EQUAL:
            comparisonOpEnum = ComparisonOperator.Enum.GET;
            break;
        case GREATER_THAN:
            comparisonOpEnum = ComparisonOperator.Enum.GT;
            break;
        case LOWER_OR_EQUAL:
            comparisonOpEnum = ComparisonOperator.Enum.LET;
            break;
        case LOWER_THAN:
            comparisonOpEnum = ComparisonOperator.Enum.LT;
            break;
        default:
            throw new IllegalStateException("Unsupported comparison operator: " + elNode.getImage());
        }

        return buildNewAST(new ComparisonExpression(new ComparisonOperator(comparisonOpEnum), leftTqlElement, rightTqlElement));
    }

    @Override
    public TqlElement visitAddOp(ELNode elNode) {
        throw new IllegalStateException("Unsupported arithmetic operator: " + elNode.getImage());
    }

    @Override
    public TqlElement visitMultOp(ELNode elNode) {
        throw new IllegalStateException("Unsupported arithmetic operator: " + elNode.getImage());
    }

    @Override
    public TqlElement visitLogicalOp(ELNode elNode) {
        switch (elNode.getType()) {
        case AND:
            return TqlBuilder.and(buildExpressionsArray(elNode));
        case OR:
            return TqlBuilder.or(buildExpressionsArray(elNode));
        case NOT:
            return visitNot(elNode);
        default:
            throw new IllegalStateException("Unsupported logical operator: " + elNode.getImage());
        }
    }

    @Override
    public TqlElement visitNot(ELNode elNode) {
        if (isLiteral(elNode.getChild(0).getType().name())) {
            throw new IllegalArgumentException("The parameters of not function cannot be a literal");
        }

        if (elNode.getChild(0).getType().equals(ELNodeType.HPATH)) {
            throw new IllegalArgumentException("The parameters of not function cannot be an hierarchical path");
        }

        return TqlBuilder.not((Expression) elNode.getChild(0).accept(this));
    }

    @Override
    public TqlElement visitHPath(ELNode elNode) {
        return new FieldReference(elNode.getImage());
    }

    @Override
    public Expression visitVariable(ELNode elNode) {
        /*
         * DSEL supports this since V1.1.0 but not TQL
         */
        throw new IllegalStateException("Unsupported Variable: " + elNode.getImage());
    }

    @Override
    public TqlElement visitFunctionCall(ELNode elNode) {
        final String fieldName = elNode.getChild(0).getImage();

        switch (elNode.getImage()) {
        case IsEmpty.NAME:
            return TqlBuilder.isEmpty(fieldName);
        case IsNull.NAME:
            return TqlBuilder.isNull(fieldName);
        case Matches.NAME:
            return TqlBuilder.match(fieldName, elNode.getChild(1).getImage());
        case Contains.NAME:
            final String value = elNode.getChild(1).getImage();
            final ELNode isCaseSensitiveNode = elNode.getFirstChild(ELNodeType.BOOLEAN_LITERAL);
            return (isCaseSensitiveNode != null && "false".equals(isCaseSensitiveNode.getImage()))
                    ? TqlBuilder.containsIgnoreCase(fieldName, value)
                    : TqlBuilder.contains(fieldName, value);
        case Between.NAME: {
            return visitBetweenFunction(elNode);
        }
        case BUILTIN_FUNCTION_IN: {
            return visitInFunction(elNode);
        }
        case BUILTIN_FUNCTION_IS_VALID:
            return visitValidFunction(elNode);
        case BUILTIN_FUNCTION_IS_INVALID:
            return visitInvalidFunction(elNode);
        case FAKE_FUNCTION_HAS_INVALID:
            return visitFakeInvalidFunction();
        case FAKE_FUNCTION_HAS_EMPTY:
            return visitFakeEmptyFunction();
        case BUILTIN_FUNCTION_COMPLIES:
            return visitCompliesFunction(elNode);
        case BUILTIN_FUNCTION_WORD_COMPLIES:
            return visitWordCompliesFunction(elNode);
        default:
            throw new IllegalStateException("Unsupported function: " + elNode.getImage());
        }
    }

    @Override
    public TqlElement visitAssignment(ELNode elNode) {
        /*
         * `field_name=123` is considered as an assignment, despite this is the following which should be an assignment:
         * `let field_name=123`. Finally we decided to consider this case as an equality.
         */
        return TqlBuilder.eq(elNode.getChild(0).getImage(), elNode.getChild(1).getImage());
    }

    @Override
    public TqlElement visitIfThenElse(ELNode elNode) {
        /*
         * DSEL supports this since V1.1.0 but not yet TQL
         */
        throw new IllegalStateException("Unsupported conditional expression : " + elNode.getImage());
    }

    @Override
    public TqlElement visitSwitchCases(ELNode elNode) {
        /*
         * DSEL supports this since V1.1.0 but not TQL
         */
        throw new IllegalStateException("Unsupported conditional expression : " + elNode.getImage());
    }

    @Override
    public TqlElement visitExternal(ELNode elNode) {
        throw new IllegalStateException("Unsupported external expression : " + elNode.getImage());
    }

    private Expression[] buildExpressionsArray(ELNode elNode) {
        List<Expression> list = new ArrayList<>();
        for (ELNode child : elNode.getChildren()) {
            Expression accept = (Expression) child.accept(this);
            list.add(accept);
        }
        return list.toArray(new Expression[0]);
    }

    private TqlElement visitBetweenFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        final ELNode leftParameter = elNode.getChild(1);
        final ELNode rightParameter = elNode.getChild(2);

        if (!isLiteral(leftParameter.getType().name()) || !isLiteral(rightParameter.getType().name())) {
            throw new IllegalArgumentException(
                    "One or two of the parameters is not a literal, whereas the between function can only accept literals");
        }

        final LiteralValue literalValueOnLeft = (LiteralValue) visitLiteral(leftParameter);
        final LiteralValue literalValueOnRight = (LiteralValue) visitLiteral(rightParameter);

        return buildNewAST(new FieldBetweenExpression(field, literalValueOnLeft, literalValueOnRight, false, false));
    }

    private TqlElement visitInFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        final List<ELNode> children = elNode.getChildren();

        final List<LiteralValue> literalValues = new ArrayList<>();

        // The first child is the field name, so we need to skip it then start index to 1.
        children.stream().skip(1L).forEach(currChild -> {
            if (!isLiteral(currChild.getType().name())) {
                throw new IllegalArgumentException(
                        "At least one of the parameters is not a literal, whereas the in function can only accept literals");
            }
            literalValues.add((LiteralValue) visitLiteral(currChild));
        });

        return buildNewAST(new FieldInExpression(field, literalValues.toArray(new LiteralValue[] {})));
    }

    private TqlElement visitValidFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        return buildNewAST(new FieldIsValidExpression(field));
    }

    private TqlElement visitInvalidFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        return buildNewAST(new FieldIsInvalidExpression(field));
    }

    private TqlElement visitFakeInvalidFunction() {
        return buildNewAST(new FieldIsInvalidExpression(new AllFields()));
    }

    private TqlElement visitFakeEmptyFunction() {
        return buildNewAST(new FieldIsEmptyExpression(new AllFields()));
    }

    private TqlElement visitCompliesFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        final ELNode fieldTypeNode = elNode.getChild(1);

        if (!ELNodeType.STRING_LITERAL.equals(fieldTypeNode.getType())) {
            throw new IllegalArgumentException(
                    "The second parameter is not a STRING literal, whereas the complies function can only accept a STRING literal as second parameter");
        }

        final LiteralValue fieldTypeLiteralValue = (LiteralValue) visitLiteral(fieldTypeNode);

        return buildNewAST(new FieldCompliesPattern(field, fieldTypeLiteralValue.getValue()));
    }

    private TqlElement visitWordCompliesFunction(ELNode elNode) {
        final TqlElement field = elNode.getChild(0).accept(this);
        final ELNode fieldTypeNode = elNode.getChild(1);

        if (!ELNodeType.STRING_LITERAL.equals(fieldTypeNode.getType())) {
            throw new IllegalArgumentException(
                    "The second parameter is not a STRING literal, whereas the wordComplies function can only accept a STRING literal as second parameter");
        }

        final LiteralValue fieldTypeLiteralValue = (LiteralValue) visitLiteral(fieldTypeNode);

        return buildNewAST(new FieldWordCompliesPattern(field, fieldTypeLiteralValue.getValue()));
    }

    static boolean isLiteral(String tokenType) {
        return Arrays.asList(DSELConstants.LITERAL_TYPES).contains(tokenType)
                || ELNodeType.BOOLEAN_LITERAL.name().equals(tokenType);
    }

    private OrExpression buildNewAST(Expression expr) {
        // Adding it to a new AST
        AndExpression andExpression = new AndExpression(expr);
        return new OrExpression(andExpression);
    }
}
