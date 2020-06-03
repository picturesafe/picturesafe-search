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
import de.picturesafe.search.parameter.aggregation.DateRangeAggregation;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.DateRangeAggregationBuilder;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticAggregationUtils.aggregationName;
import static de.picturesafe.search.parameter.aggregation.DateRangeAggregation.Range;

public class DateRangeAggregationBuilderFactory implements AggregationBuilderFactory<DateRangeAggregation>, TimeZoneAware {

    @Override
    public List<AggregationBuilder> create(DateRangeAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final DateRangeAggregationBuilder dateRangeAggregationBuilder = AggregationBuilders
                .dateRange(aggregationName(aggregation))
                .field(aggregation.getField())
                .timeZone(ZoneId.of(getTimeZone()));

        if (StringUtils.isNotBlank(aggregation.getFormat())) {
            dateRangeAggregationBuilder.format(aggregation.getFormat());
        }

        for (final Range range : aggregation.getRanges()) {
            dateRangeAggregationBuilder.addRange(range.getKey(), range.getFrom(), range.getTo());
        }
        return Collections.singletonList(dateRangeAggregationBuilder);
    }

    @Override
    public Class<DateRangeAggregation> getAggregationType() {
        return DateRangeAggregation.class;
    }
}
