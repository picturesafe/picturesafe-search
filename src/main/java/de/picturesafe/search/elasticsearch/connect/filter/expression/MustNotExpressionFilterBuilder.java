/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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

        final MustNotExpression mustNotExpression = (MustNotExpression) expressionFilterBuilderContext.getExpression();
        final QueryDto queryDto = expressionFilterBuilderContext.getQueryDto();
        final MappingConfiguration mappingConfiguration = expressionFilterBuilderContext.getMappingConfiguration();

        final ExpressionFilterFactory expressionFilterFactory = expressionFilterBuilderContext.getInitiator();
        final QueryBuilder filterBuilder = expressionFilterFactory.buildFilter(mustNotExpression.getExpression(), queryDto, mappingConfiguration);
        return QueryBuilders.boolQuery().mustNot(filterBuilder);
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        return expressionFilterBuilderContext.getExpression() instanceof MustNotExpression;
    }
}
