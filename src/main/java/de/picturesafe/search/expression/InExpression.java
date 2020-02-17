/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.expression.internal.FalseExpression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Expression to match a set of values
 */
public class InExpression extends AbstractExpression implements FieldExpression {

    private String name = "";
    private Object[] values;

    /**
     * Default constructor
     */
    public InExpression() {
    }

    /**
     * Constructor
     * @param name Field name
     * @param values Values to match
     */
    public InExpression(String name, Object... values) {
        this.name = name;
        this.values = values;
    }

    /**
     * Constructor
     * @param name Field name
     * @param ids IDs to match
     */
    public InExpression(String name, int[] ids) {
        this.name = name;
        this.values = ArrayUtils.toObject(ids);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the field name
     * @param name Filed name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the values to match
     * @return Values to match
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Sets the values to match
     * @param values Values to match
     */
    public void setValues(Object[] values) {
        this.values = values;
    }

    /**
     * Sets the IDs to match
     * @param ids IDs to match
     */
    public void setIds(int[] ids) {
        this.values = ArrayUtils.toObject(ids);
    }

    @Override
    public Expression optimize() {
        if (values != null && values.length == 0) {
            return new FalseExpression();
        } else {
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InExpression that = (InExpression) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(values, that.values)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("name", name)
                .append("values", valuesToString(10))
                .toString();
    }

    private Object valuesToString(int maxElements) {
        if (ArrayUtils.isNotEmpty(values) && values.length > maxElements) {
            final Object[] subArray = ArrayUtils.subarray(values, 0, maxElements);
            final String suffix = (values.length > subArray.length) ? ",..." : "";
            return "[" + ArrayUtils.toString(subArray) + suffix + "]";
        } else {
            return values;
        }
    }
}
