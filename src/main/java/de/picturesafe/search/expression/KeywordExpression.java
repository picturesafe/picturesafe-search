/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;

/**
 * Expression to match values on keyword fields
 *
 * @see ConditionExpression
 */
public class KeywordExpression extends ValueExpression {

    /**
     * Default constructor
     */
    public KeywordExpression() {
    }

    /**
     * Constructor
     * @param name Field name
     * @param value Value to match
     */
    public KeywordExpression(String name, Object value) {
        this(name, EQ, value);
    }

    /**
     * Constructor
     * @param name Field name
     * @param comparison Comparison operation
     * @param value Value to match
     */
    public KeywordExpression(String name, Comparison comparison, Object value) {
        super(name, comparison, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeywordExpression)) {
            return false;
        }

        final KeywordExpression that = (KeywordExpression) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .appendSuper(super.toString())
                .toString();
    }
}
