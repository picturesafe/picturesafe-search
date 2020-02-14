/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.IsNullExpression;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

public class IsNullExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder {

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof IsNullExpression)) {
            return null;
        }

        final IsNullExpression isNullExpression = (IsNullExpression) context.getExpression();
        final String esFieldName
                = FieldConfigurationUtils.getElasticFieldName(context.getMappingConfiguration(), isNullExpression.getName(), context.getQueryDto().getLocale());
        final ExistsQueryBuilder existsQuery = new ExistsQueryBuilder(esFieldName);
        return isNullExpression.isMatchNull() ? new BoolQueryBuilder().mustNot(existsQuery) : existsQuery;
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext context) {
        if (context.getExpression() instanceof IsNullExpression) {
            return hasFieldConfiguration(context);
        } else {
            return false;
        }
    }
}
