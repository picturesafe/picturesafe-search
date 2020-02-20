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

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFilterDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates In or NotIn filter expressions.
 * <p>
 * The value set by the parameter {@link #queryFieldName} determines for which key from the {@link QueryDto}s
 * this {@link FilterFactory} is responsible.
 * </p>
 * <p>
 * If the value {@link #elasticsearchInFieldName} is set, all corresponding values in the {@link QueryDto}
 * creates filter expressions that check for the existence of the value in the Elasticsearch index.
 * </p>
 * <p>
 * If the value {@link #elasticsearchNotInFieldName} is set, filter expressions are created for all corresponding
 * values in the {@link QueryDto}, which check for an non-existence of the value in the Elasticsearch index.
 * </p>
 * <p>
 * If the value {@link #elasticsearchMissingValueAllowed} is set to true, the filter will also leave missing values in the
 * Elasticsearch index, otherwise a missing value in the index leads to an unfulfilled filter criterion.
 */
public class InFilterFactory implements FilterFactory {

    private String queryFieldName, elasticsearchInFieldName;
    private String elasticsearchNotInFieldName;
    private boolean elasticsearchMissingValueAllowed;

    public void setElasticsearchInFieldName(String elasticsearchInFieldName) {
        this.elasticsearchInFieldName = elasticsearchInFieldName;
    }

    public void setElasticsearchMissingValueAllowed(boolean elasticsearchMissingValueAllowed) {
        this.elasticsearchMissingValueAllowed = elasticsearchMissingValueAllowed;
    }

    public void setElasticsearchNotInFieldName(String elasticsearchNotInFieldName) {
        this.elasticsearchNotInFieldName = elasticsearchNotInFieldName;
    }

    public void setQueryFieldName(String queryFieldName) {
        this.queryFieldName = queryFieldName;
    }


    @Override
    public List<QueryBuilder> create(final QueryDto queryDto, final MappingConfiguration mappingConfiguration) {
        final List<QueryBuilder> result = new ArrayList<>();

        final List<QueryFilterDto> filters = queryDto.getQueryFilterDtos();
        if (CollectionUtils.isNotEmpty(filters)) {
            // The values contained in all QueryFilterDTOs with the corresponding key are stored in a list.
            boolean filterApplied = false;
            final List<Object> values = new ArrayList<>();
            for (QueryFilterDto queryFilter : filters) {
                if (queryFilter.getKey().equals(queryFieldName)) {
                    filterApplied = true;
                    final Object value = queryFilter.getValue();
                    if (value != null) {
                        if (value instanceof Collection) {
                            // The collection can also be empty, but then it should still be possible to query it, so there is "filterApplied"
                            values.addAll((Collection) value);
                        } else {
                            values.add(value);
                        }
                    }
                }
            }

            if (!filterApplied) {
                return result;
            }

            if (values.isEmpty()) {
                if (elasticsearchMissingValueAllowed) {
                    if (StringUtils.hasText(elasticsearchInFieldName)) {
                        result.add(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(elasticsearchInFieldName)));
                    }
                    if (StringUtils.hasText(elasticsearchNotInFieldName)) {
                        result.add(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(elasticsearchNotInFieldName)));
                    }
                }
            } else {
                if (StringUtils.hasText(elasticsearchInFieldName)) {
                    // An In-Expression is to be created, if necessary with an "OrMissing" construct!
                    result.add(wrapOrMissingFilter(inFilter(elasticsearchInFieldName, values), elasticsearchInFieldName));
                }
                if (StringUtils.hasText(elasticsearchNotInFieldName)) {
                    // A NotIn expression is to be generated, if necessary with an "OrMissing" construct!
                    final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().mustNot(inFilter(elasticsearchNotInFieldName, values));
                    result.add(wrapOrMissingFilter(boolQueryBuilder, elasticsearchNotInFieldName));
                }
            }
        }

        return result;
    }

    @Override
    public boolean canHandleSearch(final QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        return false;
    }

    private QueryBuilder inFilter(final String fieldName, final List<Object> values) {
        Validate.notEmpty(fieldName, "fieldname must be not empty");
        Validate.notEmpty(values, "list of values must be not empty");
        final Object[] valueArray = values.toArray(new Object[0]);
        return inFilter(fieldName, valueArray);
    }

    private QueryBuilder wrapOrMissingFilter(final QueryBuilder filterBuilder, final String fieldName) {
        if (filterBuilder != null && elasticsearchMissingValueAllowed) {
            // If the corresponding field is missing in the Elasticsearch index, then the filter condition should also be fulfilled!
            return QueryBuilders.boolQuery()
                    .should(filterBuilder)
                    .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(fieldName)));

        } else {
            return filterBuilder;
        }
    }

    private QueryBuilder inFilter(String key, Object value) {
        if (value == null) {
            return null;
        }
        Validate.isTrue(value.getClass().isArray());

        if (value instanceof int[]) {
            return QueryBuilders.termsQuery(key, (int[]) value);
        } else if (value instanceof long[]) {
            return QueryBuilders.termsQuery(key, (long[]) value);
        } else if (value instanceof float[]) {
            return QueryBuilders.termsQuery(key, (float[]) value);
        } else if (value instanceof double[]) {
            return QueryBuilders.termsQuery(key, (double[]) value);
        } else if (value instanceof String[]) {
            return QueryBuilders.termsQuery(key, (String[]) value);
        } else if (value instanceof Object[]) {
            return QueryBuilders.termsQuery(key, (Object[]) value);
        } else {
            throw new InvalidParameterException("Invalid parameter object type for inQuery: " + value.getClass().getName());
        }
    }
}
