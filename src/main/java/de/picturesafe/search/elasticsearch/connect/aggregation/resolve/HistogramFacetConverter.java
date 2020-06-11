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

package de.picturesafe.search.elasticsearch.connect.aggregation.resolve;

import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.FacetEntryDto;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistogramFacetConverter implements FacetConverter {

    @Override
    public boolean isResponsible(Aggregation aggregation) {
        return aggregation instanceof Histogram;
    }

    @Override
    public FacetDto convert(Aggregation aggregation, FacetResolver facetResolver, Locale locale) {
        final Histogram histogram = (Histogram) aggregation;
        final String originalFacetName = StringUtils.substringBefore(histogram.getName(), ".");

        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            if (bucket.getDocCount() == 0) {
                continue;
            }

            final String value;
            if (facetResolver != null) {
                final long time = ((ZonedDateTime) bucket.getKey()).toInstant().toEpochMilli();
                value = facetResolver.resolve(bucket.getKeyAsString(), time, locale);
            } else {
                value = bucket.getKeyAsString();
            }

            facetEntryDtos.add(new FacetEntryDto(value, bucket.getDocCount()));
            totalCount += bucket.getDocCount();
        }
        return new FacetDto(originalFacetName, totalCount, facetEntryDtos);
    }
}
