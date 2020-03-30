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

package de.picturesafe.search.elasticsearch.connect.filter.util;

import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FilterFactoryUtils {

    private FilterFactoryUtils() {
    }

    public static QueryBuilder createFilter(List<FilterFactory> filterFactories, SearchContext context) {
        final List<QueryBuilder> queryBuilders = new ArrayList<>();
        for (FilterFactory filterFactory : filterFactories) {
            final List<QueryBuilder> filters = filterFactory.create(context);
            if (CollectionUtils.isNotEmpty(filters)) {
                queryBuilders.addAll(filters);
            }
        }
        return combine(queryBuilders);
    }

    private static QueryBuilder combine(List<QueryBuilder> queryBuilders) {
        queryBuilders = queryBuilders.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (queryBuilders.size() == 0) {
            return null;
        } else if (queryBuilders.size() == 1) {
            return queryBuilders.get(0);
        } else {
            final BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
            queryBuilders.forEach(boolFilter::filter);
            return boolFilter;
        }
    }
}
