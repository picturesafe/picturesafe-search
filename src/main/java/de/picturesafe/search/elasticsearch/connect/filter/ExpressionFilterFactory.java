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

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilderContext;
import de.picturesafe.search.expression.Expression;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

public class ExpressionFilterFactory implements FilterFactory {

    private final List<ExpressionFilterBuilder> expressionFilterBuilders;

    public ExpressionFilterFactory(List<ExpressionFilterBuilder> expressionFilterBuilders) {
        this.expressionFilterBuilders = expressionFilterBuilders;
    }

    @Override
    public List<QueryBuilder> create(QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final List<QueryBuilder> result = new ArrayList<>();
        result.add(buildFilter(queryDto.getExpression(), queryDto, mappingConfiguration));

        return result;
    }

    public QueryBuilder buildFilter(Expression expression, QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final ExpressionFilterBuilderContext expressionFilterBuilderContext = new ExpressionFilterBuilderContext(
                expression, queryDto, mappingConfiguration, this
        );
        for (ExpressionFilterBuilder expressionFilterBuilder : expressionFilterBuilders) {
            final QueryBuilder filterBuilder = expressionFilterBuilder.buildFilter(expressionFilterBuilderContext);
            if (filterBuilder != null) {
                return filterBuilder;
            }
        }

        return null;
    }

    @Override
    public boolean canHandleSearch(QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final ExpressionFilterBuilderContext expressionFilterBuilderContext = new ExpressionFilterBuilderContext(
                queryDto.getExpression(), queryDto, mappingConfiguration, this
        );
        for (ExpressionFilterBuilder expressionFilterBuilder : expressionFilterBuilders) {
            if (expressionFilterBuilder.canHandleSearch(expressionFilterBuilderContext)) {
                return true;
            }
        }

        return false;
    }
}
