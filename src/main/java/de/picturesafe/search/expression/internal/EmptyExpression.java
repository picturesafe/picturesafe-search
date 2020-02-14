/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression.internal;

import de.picturesafe.search.expression.AbstractExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An expression without a meaning which will be removed on optimizations. The use of this expression is to prevent code from returning null in cases where
 * no expression would be created.
 */
public class EmptyExpression extends AbstractExpression {

    @Override
    public Expression optimize() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmptyExpression;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .toString();
    }
}
