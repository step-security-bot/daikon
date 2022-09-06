package org.talend.tql.model;

import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.talend.tql.visitor.IASTVisitor;

/*
 * Tql expression for field in the set of the specified values.
 */

/**
 * Created by bguillon on 23/06/16.
 */
public class FieldInExpression implements Atom {

    private final TqlElement field;

    private final LiteralValue[] values;

    public FieldInExpression(TqlElement field, LiteralValue[] values) {
        this.field = field;
        this.values = values;
    }

    public TqlElement getField() {
        return field;
    }

    public LiteralValue[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "FieldInExpression{" + "field='" + field + '\'' + ", values=" + Arrays.toString(values) + '}';
    }

    @Override
    public String toQueryString() {
        StringBuilder sb = new StringBuilder();

        Arrays.stream(values).limit(values.length - 1).map(expression -> expression.toQueryString() + ", ").forEach(sb::append);
        Arrays.stream(values).skip(values.length - 1).map(TqlElement::toQueryString).forEach(sb::append);

        return field.toQueryString() + " in [" + sb + "]";
    }

    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object expression) {
        return expression instanceof FieldInExpression
                && new EqualsBuilder().append(field, ((FieldInExpression) expression).field)
                        .append(values, ((FieldInExpression) expression).values).isEquals();
    }
}
