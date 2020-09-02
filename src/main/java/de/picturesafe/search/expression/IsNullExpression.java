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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Expression to match null values
 */
public class IsNullExpression extends AbstractExpression implements FieldExpression {

    private String name;
    private boolean matchNull = true;

    /**
     * Default constructor
     */
    public IsNullExpression() {
    }

    /**
     * Constructor
     * @param name Name of the field
     */
    public IsNullExpression(String name) {
        this.name = name;
    }

    /**
     * Constructor
     * @param name Name of the field
     * @param matchNull TRUE: match NULL values, FALSE: match NOT NULL values
     */
    public IsNullExpression(String name, boolean matchNull) {
        this.name = name;
        this.matchNull = matchNull;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name of the field
     * @param name Name of the field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets if this expression matches null values.
     * @return TRUE: match NULL values, FALSE: match NOT NULL values (default = true)
     */
    public boolean isMatchNull() {
        return matchNull;
    }

    /**
     * Sets if this expression matches null values.
     * @param matchNull TRUE: match NULL values, FALSE: match NOT NULL values (default = true)
     */
    public void setMatchNull(boolean matchNull) {
        this.matchNull = matchNull;
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
        if (!(o instanceof IsNullExpression)) {
            return false;
        }

        final IsNullExpression that = (IsNullExpression) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(matchNull, that.matchNull)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(matchNull)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("name", name)
                .append("matchNull", matchNull)
                .toString();
    }
}
