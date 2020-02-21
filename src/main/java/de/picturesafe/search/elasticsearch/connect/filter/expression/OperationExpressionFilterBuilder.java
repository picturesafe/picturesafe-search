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

import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.ExpressionFilterFactory;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

public class OperationExpressionFilterBuilder implements ExpressionFilterBuilder {

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        if (!(expressionFilterBuilderContext.getExpression() instanceof OperationExpression)) {
            return null;
        }

        final OperationExpression operationExpression = (OperationExpression) expressionFilterBuilderContext.getExpression();
        final QueryDto queryDto = expressionFilterBuilderContext.getQueryDto();
        final MappingConfiguration mappingConfiguration = expressionFilterBuilderContext.getMappingConfiguration();

        final ExpressionFilterFactory expressionFilterFactory = expressionFilterBuilderContext.getInitiator();
        final List<Expression> operands = operationExpression.getOperands();
        final List<QueryBuilder> queryBuilders = new ArrayList<>();
        for (final Expression operand : operands) {
            final QueryBuilder filterBuilder = expressionFilterFactory.buildFilter(operand, queryDto, mappingConfiguration);
            if (filterBuilder != null) {
                queryBuilders.add(filterBuilder);
            }
        }
        if (queryBuilders.size() > 0) {
            if (queryBuilders.size() == 1) {
                return queryBuilders.get(0);
            } else {
                final BoolQueryBuilder bool = QueryBuilders.boolQuery();
                for (QueryBuilder queryBuilder: queryBuilders) {
                    switch (operationExpression.getOperator()) {
                        case AND:
                            bool.filter(queryBuilder);
                            break;
                        case OR:
                            bool.should(queryBuilder);
                            break;
                    }
                }
                return bool;
            }
        }

        return null;
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        final Expression expression = expressionFilterBuilderContext.getExpression();
        final QueryDto queryDto = expressionFilterBuilderContext.getQueryDto();
        final MappingConfiguration mappingConfiguration = expressionFilterBuilderContext.getMappingConfiguration();
        if (expression instanceof OperationExpression) {
            final List<Expression> operands = ((OperationExpression) expression).getOperands();
            boolean result = true;
            for (Expression subExpression : operands) {
                final QueryDto fake = new QueryDto(subExpression, queryDto.getQueryRangeDto(), queryDto.getQueryFilterDtos(), queryDto.getSortOptions(),
                        queryDto.getQueryFacetDtos(), queryDto.getLocale(), queryDto.getFieldsToResolve(), queryDto.getFieldResolverType());
                result = result && expressionFilterBuilderContext.getInitiator().canHandleSearch(fake, mappingConfiguration);
            }

            return result;
        }

        return false;
    }
}
