/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.Validate;
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

    protected String name;
    protected Comparison comparison;

    /**
     * Constructor
     * @param comparison Comparison operation
     */
    protected ConditionExpression(Comparison comparison) {
        Validate.notNull(comparison, "Parameter 'comparison' may not be null!");
        this.comparison = comparison;
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
     * @param name Field name
     * @param comparison Comparison operation
     */
    protected ConditionExpression(String name, Comparison comparison) {
        Validate.notEmpty(name, "Parameter 'name' may not be null or empty!");
        Validate.notNull(comparison, "Parameter 'comparison' may not be null!");
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
