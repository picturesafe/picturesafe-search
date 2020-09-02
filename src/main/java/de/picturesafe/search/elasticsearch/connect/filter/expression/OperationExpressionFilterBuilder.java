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

import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import de.picturesafe.search.elasticsearch.connect.filter.ExpressionFilterFactory;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

public class OperationExpressionFilterBuilder implements ExpressionFilterBuilder {

    @Override
    public boolean supports(ExpressionFilterBuilderContext context) {
        // OperationExpression could only be processed partly by OperationExpressionQueryFactory, that's why here is no processed check. It doesn't matter if it
        // has been processed fully because the operands will be marked as processed.
        return context.getExpression() instanceof OperationExpression;
    }

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        final OperationExpression operationExpression = (OperationExpression) context.getExpression();
        final SearchContext searchContext = context.getSearchContext();

        final ExpressionFilterFactory expressionFilterFactory = context.getInitiator();
        final List<Expression> operands = operationExpression.getOperands();
        final List<QueryBuilder> filterBuilders = new ArrayList<>();
        for (final Expression operand : operands) {
            final QueryBuilder filterBuilder = expressionFilterFactory.buildFilter(operand, searchContext);
            if (filterBuilder != null) {
                filterBuilders.add(filterBuilder);
            }
        }

        QueryBuilder queryBuilder = null;
        if (filterBuilders.size() > 0) {
            if (filterBuilders.size() == 1) {
                queryBuilder = filterBuilders.get(0);
            } else {
                final BoolQueryBuilder bool = QueryBuilders.boolQuery();
                for (QueryBuilder filter: filterBuilders) {
                    switch (operationExpression.getOperator()) {
                        case AND:
                            bool.must(filter);
                            break;
                        case OR:
                            bool.should(filter);
                            break;
                    }
                }
                queryBuilder = bool;
            }
        }

        if (queryBuilder != null) {
            context.setProcessed();
        }
        return queryBuilder;
    }
}
