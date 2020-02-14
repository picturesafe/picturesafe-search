/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.BETWEEN;

/**
 * Expression to search ranges between two values
 */
public class RangeValueExpression extends ConditionExpression {

    private Object minValue;
    private Object maxValue;

    /**
     * Default constructor
     */
    public RangeValueExpression() {
        super.setComparison(BETWEEN);
    }

    /**
     * Constructor
     * @param name Field name
     * @param minValue Minimum value
     * @param maxValue Maximum value
     */
    public RangeValueExpression(String name, Object minValue, Object maxValue) {
        super(name, BETWEEN);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Gets the minimum value
     * @return Minimum value
     */
    public Object getMinValue() {
        return minValue;
    }

    /**
     * Sets the minimum value
     * @param minValue Minimum value
     */
    public void setMinValue(Object minValue) {
        this.minValue = minValue;
    }

    /**
     * Gets the maximum value
     * @return Maximum value
     */
    public Object getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maximum value
     * @param maxValue Maximum value
     */
    public void setMaxValue(Object maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RangeValueExpression that = (RangeValueExpression) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(minValue, that.minValue)
                .append(maxValue, that.maxValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(minValue)
                .append(maxValue)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .appendSuper(super.toString())
                .append("minValue", minValue)
                .append("maxValue", maxValue)
                .toString();
    }
}
