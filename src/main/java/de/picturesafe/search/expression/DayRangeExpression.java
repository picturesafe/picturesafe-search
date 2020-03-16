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
import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.BETWEEN;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LE;

/**
 * Expression to query day ranges where the time of day does not matter. If only one of the day boundaries is given, an open range will be assumed.
 *
 * Examples:
 * <ul>
 * <li>
 * <pre>fromDay != null &amp;&amp;  untilDay != null</pre>
 * Produces: <pre>fromDay &lt;= VALUE  &amp;&amp;  untilDay &gt;= VALUE</pre>
 * </li>
 * <li>
 * <pre>fromDay != null &amp;&amp;  untilDay == null</pre>
 * Producesu: <pre>fromDay &lt;= VALUE</pre>
 * </li>
 * <li>
 * <pre>fromDay == null &amp;&amp;  untilDay != null</pre>
 * Produces: <pre>untilDay &gt;= VALUE</pre>
 * </li>
 * <li>
 * <pre>fromDay == null &amp;&amp;  untilDay == null</pre>
 * Produces: <pre>TRUE</pre>
 * </li>
 * </ul>
 */
public final class DayRangeExpression extends ConditionExpression {

    private Date fromDay;
    private Date untilDay;

    /**
     * Default constructor
     */
    public DayRangeExpression() {
        super(BETWEEN);
    }

    /**
     * Default constructor
     * @param name Field name
     * @param fromDay Start of the range
     * @param untilDay End of the range
     */
    public DayRangeExpression(String name, Date fromDay, Date untilDay) {
        super(name, BETWEEN);

        setFromDay(fromDay);
        setUntilDay(untilDay);
    }

    /**
     * Gets the start of the range
     * @return Start of the range
     */
    public Date getFromDay() {
        return (fromDay != null) ? new Date(fromDay.getTime()) : null;
    }

    /**
     * Sets the start of the range
     * @param fromDay Start of the range
     */
    public void setFromDay(Date fromDay) {
        this.fromDay = (fromDay != null) ? DateUtils.truncate(fromDay, Calendar.DAY_OF_MONTH) : null;
    }

    /**
     * Gets the end of the range
     * @return End of the range
     */
    public Date getUntilDay() {
        return (untilDay != null) ? new Date(untilDay.getTime()) : null;
    }

    /**
     * Sets the end of the range
     * @param untilDay End of the range
     */
    public void setUntilDay(Date untilDay) {
        this.untilDay = (untilDay != null) ? DateUtils.truncate(untilDay, Calendar.DAY_OF_MONTH) : null;
    }

    @Override
    public Expression optimize() {
        final Expression optimizedExpression;
        if (fromDay != null && untilDay != null) {
            optimizedExpression = this;
        } else {
            if (fromDay == null && untilDay == null) {
                optimizedExpression = null;
            } else if (untilDay == null) {
                optimizedExpression = new DayExpression(getName(), GE, fromDay);
            } else {
                optimizedExpression = new DayExpression(getName(), LE, untilDay);
            }
        }

        return optimizedExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DayRangeExpression that = (DayRangeExpression) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(fromDay, that.fromDay)
                .append(untilDay, that.untilDay)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(fromDay)
                .append(untilDay)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .appendSuper(super.toString())
                .append("fromDay", fromDay)
                .append("untilDay", untilDay)
                .toString();
    }
}
