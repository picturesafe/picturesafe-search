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
 * Definition of a terms aggregation
 */
public class TermsAggregation extends AbstractAggregation<TermsAggregation> {

    public static final int DEFAULT_MIN_DOC_COUNT = 1;

    public enum Order {COUNT, KEY_ASC, KEY_DESC}

    private int maxCount;
    private Order order = Order.COUNT;
    private int minDocCount = DEFAULT_MIN_DOC_COUNT;

    /**
     * Creates a terms aggregation for the given field.
     *
     * @param field Name of the field
     * @return      Aggregation
     */
    public static TermsAggregation field(String field) {
        final TermsAggregation aggregation = new TermsAggregation();
        aggregation.field = field;
        return aggregation;
    }

    /**
     * Sets the maximum count of buckets.
     *
     * @param maxCount  Maximum count of buckets
     * @return          The aggregation
     */
    public TermsAggregation maxCount(int maxCount) {
        this.maxCount = maxCount;
        return this;
    }

    /**
     * Gets the maximum count of buckets.
     *
     * @return Maximum count of buckets
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * Sets the order of the returned buckets.
     *
     * @param order Order
     * @return      The aggregation
     */
    public TermsAggregation order(Order order) {
        this.order = order;
        return this;
    }

    /**
     * gets the order of the returned buckets.
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
    public TermsAggregation minDocCount(int minDocCount) {
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
    public TermsAggregation self() {
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

        final TermsAggregation that = (TermsAggregation) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(maxCount, that.maxCount)
                .append(minDocCount, that.minDocCount)
                .append(order, that.order)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .appendSuper(super.toString()) //--
                .append("maxCount", maxCount) //--
                .append("order", order) //--
                .append("minDocCount", minDocCount) //--
                .toString();
    }

    /**
     * Creates a terms aggregation from a default aggregation.
     *
     * @param defaultAggregation    {@link DefaultAggregation}
     * @return                      TermsAggregation
     */
    public static TermsAggregation fromDefault(DefaultAggregation defaultAggregation) {
        return TermsAggregation.field(defaultAggregation.getField()).name(defaultAggregation.getName());
    }
}
