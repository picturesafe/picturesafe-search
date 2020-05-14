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

import de.picturesafe.search.elasticsearch.timezone.TimeZoneAware;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import java.time.ZoneId;

public class DateHistogramAggregationBuilderFactory implements AggregationBuilderFactory, TimeZoneAware {

    public enum IntervalType {CALENDAR, FIXED}

    private final String interval;
    private final IntervalType intervalType;
    private String format;
    private String timeZone = DEFAULT_TIME_ZONE;
    private String name;

    public DateHistogramAggregationBuilderFactory(String interval, IntervalType intervalType) {
        this.interval = interval;
        this.intervalType = intervalType;
    }

    public DateHistogramAggregationBuilderFactory(String interval, IntervalType intervalType, String format) {
        this.interval = interval;
        this.intervalType = intervalType;
        this.format = format;
    }

    public DateHistogramAggregationBuilderFactory(String interval, IntervalType intervalType, String format, String timeZone) {
        this.interval = interval;
        this.intervalType = intervalType;
        this.format = format;
        this.timeZone = timeZone;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public AggregationBuilder createAggregationBuilder(String field, int size, String language) {
        final String aggName = StringUtils.isNotBlank(name) ? name : StringUtils.substringBefore(field, ".");
        final DateHistogramAggregationBuilder dateHistogramBuilder = AggregationBuilders.dateHistogram(aggName);
        dateHistogramBuilder.field(field);
        dateHistogramBuilder.timeZone(ZoneId.of(timeZone));
        if (StringUtils.isNotBlank(format)) {
            dateHistogramBuilder.format(format);
        }
        if (intervalType == IntervalType.CALENDAR) {
            dateHistogramBuilder.calendarInterval(new DateHistogramInterval(interval));
        } else {
            dateHistogramBuilder.fixedInterval(new DateHistogramInterval(interval));
        }
        return dateHistogramBuilder;
    }
}
