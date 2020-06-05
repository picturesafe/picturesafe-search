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

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.IndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.internal.EmptyExpression;
import de.picturesafe.search.parameter.AccountContext;
import de.picturesafe.search.parameter.SearchAggregation;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceImplTest {

    @Mock
    private Elasticsearch elasticsearch;

    @Mock
    private FieldConfigurationProvider fieldConfigurationProvider;

    @Mock
    private IndexPresetConfiguration indexPresetConfiguration;

    private ElasticsearchServiceImpl elasticsearchService;

    @Before
    public void setup() {
        when(indexPresetConfiguration.getIndexAlias()).thenReturn(getClass().getSimpleName().toLowerCase(Locale.ROOT));
        final IndexPresetConfigurationProvider indexPresetConfigurationProvider
                = new StaticIndexPresetConfigurationProvider(Collections.singletonList(indexPresetConfiguration));
        elasticsearchService = new ElasticsearchServiceImpl(elasticsearch, indexPresetConfigurationProvider, fieldConfigurationProvider);
    }

    @Test
    public void testCreateQueryDto() {
        final Expression expression = new EmptyExpression();
        final List<String> fieldsToResolve = Arrays.asList("field1", "field2", "field3");
        final List<SortOption> sortOptions = Arrays.asList(SortOption.asc("field3"), SortOption.asc("field2"));
        final List<SearchAggregation> aggregations = Arrays.asList(TermsAggregation.field("agg1").maxCount(5), TermsAggregation.field("agg2").maxCount(10));
        final int start = 11;
        final int limit = 111;
        final SearchParameter.Builder searchParameterBuilder = SearchParameter.builder()
                .language("de")
                .fieldsToResolve(fieldsToResolve)
                .sortOptions(sortOptions)
                .aggregations(aggregations);
        SearchParameter searchParameter = searchParameterBuilder.build();

        QueryDto queryDto = elasticsearchService.createQueryDto(new AccountContext(), expression, start, limit, searchParameter);
        assertEquals(expression, queryDto.getExpression());
        assertEquals(fieldsToResolve, queryDto.getFieldsToResolve());
        assertEquals(QueryDto.FieldResolverType.SOURCE_VALUES, queryDto.getFieldResolverType());
        assertEquals(sortOptions, queryDto.getSortOptions());
        assertEquals(Locale.GERMAN, queryDto.getLocale());
        final QueryRangeDto rangeDto = queryDto.getQueryRangeDto();
        assertEquals(start, rangeDto.getStart());
        assertEquals(limit, rangeDto.getLimit());
        assertEquals(searchParameter.getMaxTrackTotalHits(), rangeDto.getMaxTrackTotalHits());
        assertEquals(aggregations, queryDto.getAggregations());

        searchParameter = searchParameterBuilder.language("de_DE").build();
        queryDto = elasticsearchService.createQueryDto(new AccountContext(), expression, start, limit, searchParameter);
        assertEquals(Locale.GERMANY, queryDto.getLocale());
    }
}
