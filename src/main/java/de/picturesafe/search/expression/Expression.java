/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

/**
 * Interface of a search expression
 */
public interface Expression {

    /**
     * Optimzes the expression.
     * Removes unneeded complexity for better performance.
     *
     * @return Optimized expression
     */
    Expression optimize();

    /**
     * Gets the parent expression.
     *
     * @return Parent expression
     */
    Expression getParent();

    /**
     * Sets the parent expression.
     * @param parent Parent expression
     */
    void setParent(Expression parent);
}
