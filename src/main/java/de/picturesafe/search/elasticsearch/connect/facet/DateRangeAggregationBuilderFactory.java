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

package de.picturesafe.search.elasticsearch.connect.facet;

import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;

import java.time.ZoneId;
import java.util.List;

public class DateRangeAggregationBuilderFactory implements AggregationBuilderFactory, TimeZoneAware {

    private final List<Range> ranges;
    private String format;
    private String timeZone = DEFAULT_TIME_ZONE;
    private String name;

    public DateRangeAggregationBuilderFactory(List<Range> ranges) {
        this.ranges = ranges;
    }

    public DateRangeAggregationBuilderFactory(List<Range> ranges, String format) {
        this.ranges = ranges;
        this.format = format;
    }

    public DateRangeAggregationBuilderFactory(List<Range> ranges, String format, String timeZone) {
        this.ranges = ranges;
        this.format = format;
        this.timeZone = timeZone;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AggregationBuilder createAggregationBuilder(String field, int size, String language) {
        final String aggName = StringUtils.isNotBlank(name) ? name : StringUtils.substringBefore(field, ".");
        final DateRangeAggregationBuilder dateRangeAggregationBuilder = AggregationBuilders.dateRange(aggName);
        dateRangeAggregationBuilder.field(field);
        dateRangeAggregationBuilder.timeZone(ZoneId.of(timeZone));
        if (StringUtils.isNotBlank(format)) {
            dateRangeAggregationBuilder.format(format);
        }
        for (final Range range : ranges) {
            dateRangeAggregationBuilder.addRange(range.key, range.from, range.to);
        }
        return dateRangeAggregationBuilder;
    }

    public static class Range {

        private String key;
        private String from;
        private String to;

        public Range() {
        }

        public Range(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public Range(String key, String from, String to) {
            this.key = key;
            this.from = from;
            this.to = to;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(String to) {
            this.to = to;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                    .append("key", key) //--
                    .append("from", from) //--
                    .append("to", to) //--
                    .toString();
        }
    }
}
