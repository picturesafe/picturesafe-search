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

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.ExpressionFilterFactory;
import de.picturesafe.search.expression.MustNotExpression;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class MustNotExpressionFilterBuilder implements ExpressionFilterBuilder {

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        if (!(expressionFilterBuilderContext.getExpression() instanceof MustNotExpression)) {
            return null;
        }

        final QueryBuilder innerFilter = buildInnerFilter(expressionFilterBuilderContext);
        return (innerFilter != null) ? QueryBuilders.boolQuery().mustNot(innerFilter) : null;
    }

    QueryBuilder buildInnerFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        final MustNotExpression mustNotExpression = (MustNotExpression) expressionFilterBuilderContext.getExpression();
        final QueryDto queryDto = expressionFilterBuilderContext.getQueryDto();
        final MappingConfiguration mappingConfiguration = expressionFilterBuilderContext.getMappingConfiguration();

        final ExpressionFilterFactory expressionFilterFactory = expressionFilterBuilderContext.getInitiator();
        return expressionFilterFactory.buildFilter(mustNotExpression.getExpression(), queryDto, mappingConfiguration);
    }
}
