/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
