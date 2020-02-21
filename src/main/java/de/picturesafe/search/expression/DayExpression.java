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
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GT;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LT;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_EQ;

/**
 * Expression to match a specific day
 */
public final class DayExpression extends ConditionExpression {

    private Date day;

    /**
     * Constructor
     * @param name Field name
     * @param day Day to match
     */
    public DayExpression(String name, Date day) {
        super(name, EQ);
        setDay(day);
    }

    /**
     * Constructor
     * @param name Field name
     * @param comparison Comparison operation
     * @param day Day to match
     */
    public DayExpression(String name, Comparison comparison, Date day) {
        super(name, comparison);
        validateComparision(comparison);
        setDay(day);
    }

    /**
     * Gets the day to match
     * @return Day to match
     */
    public Date getDay() {
        return day;
    }

    /**
     * Sets the day to match
     * @param day Day to match
     */
    public void setDay(Date day) {
        if (day == null) {
            this.day = null;
        } else {
            this.day = DateUtils.truncate(day, Calendar.DAY_OF_MONTH);
        }
    }

    @Override
    public void setComparison(Comparison comparison) {
        validateComparision(comparison);
        super.setComparison(comparison);
    }

    @Override
    public Expression optimize() {
        if (day == null) {
            return null;
        } else {
            return this;
        }
    }

    private void validateComparision(Comparison comparison) {
        Validate.notNull(comparison, "Comparison may not be null!");
        if (comparison != EQ
            && comparison != NOT_EQ
            && comparison != GT
            && comparison != GE
            && comparison != LT
            && comparison != LE) {
            throw new IllegalStateException("Comparision '" + comparison + "' is not supported!");
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

        final DayExpression that = (DayExpression) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(day, that.day)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(day)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .appendSuper(super.toString())
                .append("day", day)
                .toString();
    }
}
