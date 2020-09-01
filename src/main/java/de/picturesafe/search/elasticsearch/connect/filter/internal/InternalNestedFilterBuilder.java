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

import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilderContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static de.picturesafe.search.elasticsearch.connect.util.QueryBuilderUtils.applyBoost;

public class InternalNestedFilterBuilder implements InternalFilterBuilder {

    @Override
    public QueryBuilder build(String key, Object value, ExpressionFilterBuilderContext context) {
        final QueryBuilder queryBuilder;
        if (context.getQueryDto().isSortFilter()) {
            // Build filter for nested sort
            if (value instanceof Object[]) {
                final Object[] values = (Object[]) value;
                if (values.length > 1) {
                    queryBuilder = QueryBuilders.termsQuery(key, values);
                } else {
                    queryBuilder = QueryBuilders.termQuery(key, values[0]);
                }
            } else {
                queryBuilder = QueryBuilders.termQuery(key, value);
            }
        } else {
            // Expressions on nested fields must be built as query because otherwise the score is missing!
            queryBuilder = null;
        }
        return applyBoost(queryBuilder, context.getExpression());
    }
}
