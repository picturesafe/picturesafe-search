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

package de.picturesafe.search.elasticsearch.connect.aggregation.search;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.timezone.TimeZoneAware;
import de.picturesafe.search.parameter.aggregation.DateHistogramAggregation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticAggregationUtils.aggregationName;
import static de.picturesafe.search.parameter.aggregation.DateHistogramAggregation.IntervalType.CALENDAR;

public class DateHistogramAggregationBuilderFactory implements AggregationBuilderFactory<DateHistogramAggregation>, TimeZoneAware {

    @Override
    public List<AggregationBuilder> create(DateHistogramAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final DateHistogramAggregationBuilder dateHistogramBuilder = AggregationBuilders
                .dateHistogram(aggregationName(aggregation))
                .field(aggregation.getField())
                .timeZone(ZoneId.of(getTimeZone()))
                .order(order(aggregation))
                .minDocCount(aggregation.getMinDocCount());

        if (StringUtils.isNotBlank(aggregation.getFormat())) {
            dateHistogramBuilder.format(aggregation.getFormat());
        }

        if (aggregation.getIntervalType() == CALENDAR) {
            dateHistogramBuilder.calendarInterval(new DateHistogramInterval(aggregation.getInterval()));
        } else {
            dateHistogramBuilder.fixedInterval(new DateHistogramInterval(aggregation.getInterval()));
        }
        return Collections.singletonList(dateHistogramBuilder);
    }

    private BucketOrder order(DateHistogramAggregation aggregation) {
        switch (aggregation.getOrder()) {
            case KEY_ASC:
                return BucketOrder.key(true);
            case KEY_DESC:
                return BucketOrder.key(false);
            default:
                return BucketOrder.count(false);
        }
    }

    @Override
    public Class<DateHistogramAggregation> getAggregationType() {
        return DateHistogramAggregation.class;
    }
}
