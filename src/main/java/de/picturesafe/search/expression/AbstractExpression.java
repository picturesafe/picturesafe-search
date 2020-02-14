/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

/**
 * Abstract base class for expressions
 */
public abstract class AbstractExpression implements Expression {

    private Expression parent;

    @Override
    public Expression getParent() {
        return parent;
    }

    @Override
    public void setParent(Expression parent) {
        this.parent = parent;
    }
}
