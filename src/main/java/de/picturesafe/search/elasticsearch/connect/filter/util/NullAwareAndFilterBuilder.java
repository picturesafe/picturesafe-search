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

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

public class NullAwareAndFilterBuilder {

    private final List<QueryBuilder> queryBuilders = new ArrayList<>();

    public void add(QueryBuilder filterBuilder) {
        if (filterBuilder != null) {
            queryBuilders.add(filterBuilder);
        }
    }

    public QueryBuilder toFilterBuilder() {
        if (queryBuilders.size() == 0) {
            return null;
        } else if (queryBuilders.size() == 1) {
            return queryBuilders.get(0);
        } else {
            final BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolFilter.filter(queryBuilder);
            }

            return boolFilter;
        }
    }

}
