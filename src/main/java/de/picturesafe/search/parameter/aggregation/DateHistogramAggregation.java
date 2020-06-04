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

package de.picturesafe.search.parameter.aggregation;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Definition of a date histogram aggregation
 */
public class DateHistogramAggregation extends AbstractAggregation<DateHistogramAggregation> {

    public enum IntervalType {CALENDAR, FIXED}
    public enum Order {COUNT, KEY_ASC, KEY_DESC}

    public static final IntervalType DEFAULT_INTERVAL_TYPE = IntervalType.CALENDAR;
    public static final String DEFAULT_INTERVAL = "1y";
    public static final int DEFAULT_MIN_DOC_COUNT = 1;

    private IntervalType intervalType = DEFAULT_INTERVAL_TYPE;
    private String interval = DEFAULT_INTERVAL;
    private String format;
    private Order order = Order.COUNT;
    private int minDocCount = DEFAULT_MIN_DOC_COUNT;

    /**
     * Creates a date histogram aggregation for the given field.
     *
     * @param field Name of the field
     * @return      Aggregation
     */
    public static DateHistogramAggregation field(String field) {
        final DateHistogramAggregation aggregation = new DateHistogramAggregation();
        aggregation.field = field;
        return aggregation;
    }

    /**
     * Sets the interval.
     *
     * @param intervalType  Interval type
     * @param interval      Interval
     * @return              The aggregation
     */
    public DateHistogramAggregation interval(IntervalType intervalType, String interval) {
        this.intervalType = intervalType;
        this.interval = interval;
        return this;
    }

    /**
     * Gets the interval.
     *
     * @return Interval
     */
    public String getInterval() {
        return interval;
    }

    /**
     * Gets the interval type.
     *
     * @return Interval type
     */
    public IntervalType getIntervalType() {
        return intervalType;
    }

    /**
     * Sets the format for the bucket key.
     *
     * @param format    Format for the bucket key (Java date format)
     * @return          The aggregation
     */
    public DateHistogramAggregation format(String format) {
        this.format = format;
        return this;
    }

    /**
     * Gets the format for the bucket key.
     *
     * @return  Format for the bucket key (Java date format)
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the order of the returned buckets.
     *
     * @param order Order
     * @return      The aggregation
     */
    public DateHistogramAggregation order(Order order) {
        this.order = order;
        return this;
    }

    /**
     * Gets the order of the returned buckets.
     *
     * @return Order
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Sets the minimum count of documents for buckets. Buckets containing less than the minimum count will be omitted.
     *
     * @param minDocCount   Minimum count of documents
     * @return              The aggregation
     */
    public DateHistogramAggregation minDocCount(int minDocCount) {
        this.minDocCount = minDocCount;
        return this;
    }

    /**
     * Gets the minimum count of documents for buckets.
     *
     * @return Minimum count of documents
     */
    public int getMinDocCount() {
        return minDocCount;
    }

    @Override
    protected DateHistogramAggregation self() {
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

        final DateHistogramAggregation that = (DateHistogramAggregation) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(minDocCount, that.minDocCount)
                .append(interval, that.interval)
                .append(intervalType, that.intervalType)
                .append(format, that.format)
                .append(order, that.order)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .appendSuper(super.toString()) //--
                .append("interval", interval) //--
                .append("intervalType", intervalType) //--
                .append("format", format) //--
                .append("order", order) //--
                .append("minDocCount", minDocCount) //--
                .toString();
    }

    /**
     * Creates a date histogram aggregation from a default aggregation.
     *
     * @param defaultAggregation    {@link DefaultAggregation}
     * @return                      DateHistogramAggregation
     */
    public static DateHistogramAggregation fromDefault(DefaultAggregation defaultAggregation) {
        return DateHistogramAggregation.field(defaultAggregation.getField()).name(defaultAggregation.getName()).format("yyyy");
    }
}
