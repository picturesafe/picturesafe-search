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
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TermsFacetConverter implements FacetConverter {

    @Override
    public boolean isResponsible(Aggregation aggregation) {
        return aggregation instanceof Terms;
    }

    @Override
    public FacetDto convert(Aggregation aggregation, FacetResolver facetResolver, String fieldName, Locale locale) {
        final Terms terms = (Terms) aggregation;
        final String baseFacetName = StringUtils.substringBefore(terms.getName(), ".");
        final String baseFieldName = (fieldName != null) ? StringUtils.substringBefore(fieldName, ".") : null;

        final List<FacetEntryDto> facetEntryDtos = new ArrayList<>();
        long totalCount = 0;
        for (Terms.Bucket bucket : terms.getBuckets()) {
            final long docCount = bucket.getDocCount();
            if (docCount == 0) {
                continue;
            }
            final String keyAsString = bucket.getKeyAsString();
            String value = (facetResolver != null) ? facetResolver.resolve(keyAsString, bucket.getKeyAsNumber(), locale) : keyAsString;
            if (value == null) {
                value = keyAsString;
            }

            facetEntryDtos.add(new FacetEntryDto(value, docCount));
            totalCount += docCount;
        }
        return new FacetDto(baseFacetName, baseFieldName, totalCount, facetEntryDtos);
    }
}
