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
import de.picturesafe.search.elasticsearch.connect.dto.RangeFacetEntryDto;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.range.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RangeFacetConverter implements FacetConverter {

    @Override
    public boolean isResponsible(Aggregation aggregation) {
        return aggregation instanceof Range;
    }

    @Override
    public FacetDto convert(Aggregation aggregation, FacetResolver facetResolver, String fieldName, Locale locale) {
        final Range range = (Range) aggregation;

        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        for (Range.Bucket bucket : range.getBuckets()) {
            if (bucket.getDocCount() == 0) {
                continue;
            }

            final FacetEntryDto facet;
            if (facetResolver != null) {
                final String rangeValue = bucket.getFrom() + " - " + bucket.getTo();
                final String value = facetResolver.resolve(rangeValue, null, locale);
                facet = new FacetEntryDto(value, bucket.getDocCount());
            } else {
                facet = new RangeFacetEntryDto(bucket);
            }

            facetEntryDtos.add(facet);

            totalCount += bucket.getDocCount();
        }
        return new FacetDto(aggregation.getName(), fieldName, totalCount, facetEntryDtos);
    }
}
