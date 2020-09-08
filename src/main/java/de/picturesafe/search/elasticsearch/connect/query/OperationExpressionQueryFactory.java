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

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OperationExpressionQueryFactory implements QueryFactory {

    @Override
    public boolean supports(SearchContext context) {
        return !context.isRootExpressionProcessed() && context.getRootExpression() instanceof OperationExpression;
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, SearchContext context) {
        final OperationExpression operationExpression = (OperationExpression) context.getRootExpression();
        final List<Expression> operands = operationExpression.getOperands();

        QueryBuilder queryBuilder = null;
        if (operands.size() == 1) {
            queryBuilder = caller.createQuery(new SearchContext(context, operands.get(0)));
        } else if (operands.size() > 1) {
            final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolean somethingAddedToBuilder = false;
            for (Expression operand: operands) {
                final QueryBuilder subQueryBuilder = caller.createQuery(new SearchContext(context, operand));
                if (subQueryBuilder != null) {
                    somethingAddedToBuilder = true;
                    switch (operationExpression.getOperator()) {
                        case AND:
                            boolQueryBuilder.must(subQueryBuilder);
                            break;
                        case OR:
                            boolQueryBuilder.should(subQueryBuilder);
                            break;
                    }
                }
            }

            queryBuilder = somethingAddedToBuilder ? boolQueryBuilder : null;
            if (queryBuilder != null) {
                context.setProcessed(operationExpression);
            }
        }

        return queryBuilder;
    }
}
