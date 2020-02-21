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

package de.picturesafe.search.elasticsearch.connect.impl;

import de.picturesafe.search.elasticsearch.connect.FacetResolver;
import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.FacetEntryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.RangeFacetEntryDto;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class FacetConverter {

    private final QueryDto queryDto;

    public FacetConverter(QueryDto queryDto) {
        this.queryDto = queryDto;
    }

    public FacetDto convertTermsFacet(Terms terms, FacetResolver facetResolver) {
        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        final String originalFacetName = StringUtils.substringBefore(terms.getName(), ".");
        for (Terms.Bucket bucket : terms.getBuckets()) {
            final long docCount = bucket.getDocCount();
            if (docCount == 0) {
                continue;
            }
            final String keyAsString = bucket.getKeyAsString();
            String value = (facetResolver != null)
                    ? facetResolver.resolve(keyAsString, bucket.getKeyAsNumber(), queryDto.getLocale()) : keyAsString;
            if (value == null) {
                value = keyAsString;
            }

            facetEntryDtos.add(new FacetEntryDto(value, docCount));
            totalCount += docCount;
        }
        return new FacetDto(originalFacetName, totalCount, facetEntryDtos);
    }

    public FacetDto convertRangeFacet(Range range, FacetResolver facetResolver) {
        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        for (Range.Bucket bucket : range.getBuckets()) {
            if (bucket.getDocCount() == 0) {
                continue;
            }

            final FacetEntryDto facet;
            if (facetResolver != null) {
                final String rangeValue = bucket.getFrom() + " - " + bucket.getTo();
                final String value = facetResolver.resolve(rangeValue, null, queryDto.getLocale());
                facet = new FacetEntryDto(value, bucket.getDocCount());
            } else {
                facet = new RangeFacetEntryDto(bucket);
            }

            facetEntryDtos.add(facet);

            totalCount += bucket.getDocCount();
        }
        final String originalFacetName = StringUtils.substringBefore(range.getName(), ".");
        return new FacetDto(originalFacetName, totalCount, facetEntryDtos);
    }

    public FacetDto convertHistogramFacet(Histogram histogram, FacetResolver facetResolver) {
        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        for (Histogram.Bucket bucket : histogram.getBuckets()) {
            if (bucket.getDocCount() == 0) {
                continue;
            }

            final String value;
            if (facetResolver != null) {
                final long time = ((ZonedDateTime) bucket.getKey()).toInstant().toEpochMilli();
                value = facetResolver.resolve(bucket.getKeyAsString(), time, queryDto.getLocale());
            } else {
                value = bucket.getKeyAsString();
            }

            facetEntryDtos.add(new FacetEntryDto(value, bucket.getDocCount()));
            totalCount += bucket.getDocCount();
        }
        final String originalFacetName = StringUtils.substringBefore(histogram.getName(), ".");
        return new FacetDto(originalFacetName, totalCount, facetEntryDtos);
    }
}
