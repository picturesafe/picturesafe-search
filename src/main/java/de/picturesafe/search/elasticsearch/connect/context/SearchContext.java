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

package de.picturesafe.search.elasticsearch.connect.context;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.expression.Expression;

import java.util.HashSet;
import java.util.Set;

public class SearchContext {

    private final QueryDto queryDto;
    private final MappingConfiguration mappingConfiguration;
    private final Set<Expression> processedExpressions;
    private boolean nestedQuery;

    public SearchContext(QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        this(queryDto, mappingConfiguration, new HashSet<>());
    }

    public SearchContext(QueryDto queryDto, MappingConfiguration mappingConfiguration, boolean nestedQuery) {
        this(queryDto, mappingConfiguration);
        this.nestedQuery = nestedQuery;
    }

    private SearchContext(QueryDto queryDto, MappingConfiguration mappingConfiguration, Set<Expression> processedExpressions) {
        this.queryDto = queryDto;
        this.mappingConfiguration = mappingConfiguration;
        this.processedExpressions = processedExpressions;
    }

    public SearchContext(SearchContext context, QueryDto queryDto) {
        this(queryDto, context.mappingConfiguration, context.processedExpressions);
    }

    public SearchContext(SearchContext context, Expression expression) {
        this(new QueryDto(context.queryDto, expression), context.mappingConfiguration, context.processedExpressions);
    }

    public SearchContext(SearchContext context, boolean nestedQuery) {
        this(context.queryDto, context.mappingConfiguration, nestedQuery);
    }

    public QueryDto getQueryDto() {
        return queryDto;
    }

    public MappingConfiguration getMappingConfiguration() {
        return mappingConfiguration;
    }

    public Expression getRootExpression() {
        return queryDto.getExpression();
    }

    public boolean isRootExpressionProcessed() {
        return isProcessed(queryDto.getExpression());
    }

    public boolean isProcessed(Expression expression) {
        return processedExpressions.contains(expression);
    }

    public void setProcessed(Expression expression) {
        processedExpressions.add(expression);
    }

    public boolean isNestedQuery() {
        return nestedQuery;
    }
}
