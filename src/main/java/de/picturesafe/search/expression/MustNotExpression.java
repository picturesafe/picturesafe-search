/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Expression defining a negation of another expression
 */
public class MustNotExpression extends AbstractExpression {

    private Expression expression;

    /**
     * Default constructor
     */
    public MustNotExpression() {
    }

    /**
     * Constructor
     * @param expression Expression to negate
     */
    public MustNotExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * Gets the expression to negate.
     * @return Expression to negate
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the expression to negate.
     * @param expression Expression to negate
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
        if (expression != null) {
            expression.setParent(this);
        }
    }

    @Override
    public Expression optimize() {
        return (expression != null) ? this : expression;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("expression", expression) //--
                .toString();
    }
}
