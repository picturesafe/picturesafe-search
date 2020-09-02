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

package de.picturesafe.search.elasticsearch.connect.filter.internal;

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;

import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilderContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InternalQueryFilterBuilder implements InternalFilterBuilder {

    private final QueryConfiguration queryConfig;

    public InternalQueryFilterBuilder(QueryConfiguration queryConfig) {
        this.queryConfig = queryConfig;
    }

    @Override
    public QueryBuilder build(String key, Object value, ExpressionFilterBuilderContext context) {
        return QueryBuilders.queryStringQuery(convertObject(value)).field(key)
                        .defaultOperator(queryConfig.getDefaultQueryStringOperator())
                        .analyzeWildcard(true);
    }

    private String convertObject(Object value) {
        final String result;

        if (value == null) {
            result = null;
        } else if (value instanceof Number) {
            result = value.toString();
        } else if (value instanceof String) {
            result = (String) value;
        } else {
            throw new IllegalArgumentException("Can't handle value of class " + value.getClass().getName());
        }
        return result;
    }
}
