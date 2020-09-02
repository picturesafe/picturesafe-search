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

package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.expression.BoostableExpression;
import de.picturesafe.search.expression.Expression;
import org.elasticsearch.index.query.QueryBuilder;

public class QueryBuilderUtils {

    private QueryBuilderUtils() {
    }

    public static QueryBuilder applyBoost(QueryBuilder queryBuilder, Expression expression) {
        if (queryBuilder != null && expression instanceof BoostableExpression) {
            final BoostableExpression boostableExpression = (BoostableExpression) expression;
            if (boostableExpression.getBoost() != null) {
                queryBuilder.boost(boostableExpression.getBoost());
            }
        }
        return queryBuilder;
    }
}
