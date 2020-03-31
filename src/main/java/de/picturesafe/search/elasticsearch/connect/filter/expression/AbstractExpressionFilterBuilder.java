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

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.expression.Expression;
import org.elasticsearch.index.query.QueryBuilder;

public abstract class AbstractExpressionFilterBuilder implements ExpressionFilterBuilder {

    @Override
    public final boolean supports(ExpressionFilterBuilderContext context) {
        final Expression expression = context.getExpression();
        return !context.isProcessed() && supportsExpression(expression);
    }

    protected abstract boolean supportsExpression(Expression expression);

    @Override
    public final QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        final QueryBuilder queryBuilder = buildExpressionFilter(context);
        if (queryBuilder != null) {
            context.setProcessed();
        }
        return queryBuilder;
    }

    protected abstract QueryBuilder buildExpressionFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext);
}

