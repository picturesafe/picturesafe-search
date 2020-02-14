/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Abstract base class for field or fulltext expressions
 */
public abstract class ConditionExpression extends AbstractExpression implements FieldExpression {

    public enum Comparison {
        EQ,
        NOT_EQ,
        LIKE,
        NOT_LIKE,
        GT,
        GE,
        LT,
        LE,
        BETWEEN,
        TERM_STARTS_WITH,
        TERM_ENDS_WITH,
        TERM_WILDCARD
    }

    private String name;
    private Comparison comparison;

    /**
     * Default constructor
     */
    protected ConditionExpression() {
    }

    /**
     * Constructor
     *
     * @param name Field name
     */
    protected ConditionExpression(String name) {
        this(name, Comparison.EQ);
    }

    /**
     * Constructor
     *
     * @param name Field name
     * @param comparison Comparison operation
     */
    protected ConditionExpression(String name, Comparison comparison) {
        this.name = name;
        this.comparison = comparison;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the field name
     * @param name Field name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the comparison operation
     * @return Comparison operation
     */
    public Comparison getComparison() {
        return comparison;
    }

    /**
     * Sets the comparison operation
     * @param comparison Comparison operation
     */
    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    @Override
    public Expression optimize() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ConditionExpression that = (ConditionExpression) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(comparison, that.comparison)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(comparison)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("name", name)
                .append("comparison", comparison)
                .toString();
    }
}
