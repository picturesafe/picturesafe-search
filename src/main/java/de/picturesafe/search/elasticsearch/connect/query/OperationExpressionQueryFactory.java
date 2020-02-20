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

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
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
    public boolean supports(Expression parameter) {
        return parameter instanceof OperationExpression;
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, MappingConfiguration mappingConfiguration, Expression parameter) {
        final OperationExpression operationExpression = (OperationExpression) parameter;
        final List<Expression> operands = operationExpression.getOperands();

        if (operands.size() == 1) {
            return caller.createQuery(operands.get(0), mappingConfiguration);
        } else if (operands.size() > 1) {
            final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolean somethingAddedToBuilder = false;
            for (Expression operand: operands) {
                final QueryBuilder subQueryBuilder = caller.createQuery(operand, mappingConfiguration);
                if (subQueryBuilder != null) {
                    somethingAddedToBuilder = true;
                    switch (operationExpression.getOperator()) {
                        case AND:
                            boolQueryBuilder.filter(subQueryBuilder);
                            break;
                        case OR:
                            boolQueryBuilder.should(subQueryBuilder);
                            break;
                    }
                }
            }
            return somethingAddedToBuilder ? boolQueryBuilder : null;
        }
        return null;
    }
}
