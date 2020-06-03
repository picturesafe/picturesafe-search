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

import java.util.Arrays;
import java.util.List;

/**
 * Definition of a date range aggregation
 */
public class DateRangeAggregation extends AbstractAggregation<DateRangeAggregation> {

    public static final List<Range> DEFAULT_RANGES = Arrays.asList(
            Range.from("now/d").to("now/d+1d").key("today"),
            Range.from("now/d-1d").to("now/d").key("yesterday"),
            Range.from("now/w").to("now/w+1w").key("week"),
            Range.from("now/w-1w").to("now/w").key("last week"),
            Range.from("now/M").to("now/M+1M").key("month"),
            Range.from("now/M-1M").to("now/M").key("last month")
    );

    private List<Range> ranges = DEFAULT_RANGES;
    private String format;

    /**
     * Creates a date range aggregation for the given field.
     *
     * @param field Name of the field
     * @return      Aggregation
     */
    public static DateRangeAggregation field(String field) {
        final DateRangeAggregation aggregation = new DateRangeAggregation();
        aggregation.field = field;
        return aggregation;
    }

    /**
     * Sets the date ranges.
     *
     * @param ranges    Date ranges
     * @return          The aggregation
     */
    public DateRangeAggregation ranges(List<Range> ranges) {
        this.ranges = ranges;
        return this;
    }

    /**
     * Sets the date ranges.
     *
     * @param ranges    Date ranges
     * @return          The aggregation
     */
    public DateRangeAggregation ranges(Range... ranges) {
        return ranges(Arrays.asList(ranges));
    }

    /**
     * Gets the date ranges.
     *
     * @return Date ranges
     */
    public List<Range> getRanges() {
        return ranges;
    }

    /**
     * Sets the format for the bucket key.
     *
     * @param format    Format for the bucket key (Java date format)
     * @return          The aggregation
     */
    public DateRangeAggregation format(String format) {
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

    @Override
    protected DateRangeAggregation self() {
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

        final DateRangeAggregation that = (DateRangeAggregation) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(ranges, that.ranges)
                .append(format, that.format)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .appendSuper(super.toString()) //--
                .append("ranges", ranges) //--
                .append("format", format) //--
                .toString();
    }

    /**
     * Creates a date range aggregation from a default aggregation.
     *
     * @param defaultAggregation    {@link DefaultAggregation}
     * @return                      DateRangeAggregation
     */
    public static DateRangeAggregation fromDefault(DefaultAggregation defaultAggregation) {
        return DateRangeAggregation.field(defaultAggregation.getField()).name(defaultAggregation.getName());
    }

    /**
     * Definition of a date range
     */
    public static class Range {

        private String from;
        private String to;
        private String key;

        /**
         * Sets the from date.
         *
         * @param from  From date (Elasticsearch date math format)
         * @return      The date range
         */
        public static Range from(String from) {
            final Range range = new Range();
            range.from = from;
            return range;
        }

        /**
         * Gets the from date.
         *
         * @return From date (Elasticsearch date math format)
         */
        public String getFrom() {
            return from;
        }

        /**
         * Sets only the to date.
         *
         * @param to    To date (Elasticsearch date math format)
         * @return      The date range
         */
        public static Range toOnly(String to) {
            final Range range = new Range();
            range.to = to;
            return range;
        }

        /**
         * Sets the to date.
         *
         * @param to    To date (Elasticsearch date math format)
         * @return      The date range
         */
        public Range to(String to) {
            this.to = to;
            return this;
        }

        /**
         * Gets the to date.
         *
         * @return To date (Elasticsearch date math format)
         */
        public String getTo() {
            return to;
        }

        /**
         * Sets the key for the bucket.
         *
         * @param key   Key
         * @return      The date range
         */
        public Range key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Gets the key for the bucket.
         *
         * @return Key
         */
        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                    .append("from", from) //--
                    .append("to", to) //--
                    .append("key", key) //--
                    .toString();
        }
    }
}
