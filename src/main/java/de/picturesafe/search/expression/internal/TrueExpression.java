/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression.internal;

import de.picturesafe.search.expression.AbstractExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An expression which will always result to true. This expression will be used internally in optimization processes.
 */
public class TrueExpression extends AbstractExpression {

    @Override
    public Expression optimize() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TrueExpression;
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
