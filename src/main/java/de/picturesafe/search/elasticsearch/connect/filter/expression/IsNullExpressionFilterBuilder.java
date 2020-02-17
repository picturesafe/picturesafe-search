/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.IsNullExpression;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class IsNullExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder {

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof IsNullExpression)) {
            return null;
        }

        final IsNullExpression isNullExpression = (IsNullExpression) context.getExpression();
        final MappingConfiguration mappingConfiguration = context.getMappingConfiguration();
        final String fieldName
                = FieldConfigurationUtils.getElasticFieldName(mappingConfiguration, isNullExpression.getName(), context.getQueryDto().getLocale());
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);

        QueryBuilder query = QueryBuilders.existsQuery(fieldName);
        if (fieldConfiguration != null && fieldConfiguration.isNestedObject()) {
            final String objectPath = FieldConfigurationUtils.rootFieldName(fieldConfiguration);
            query = QueryBuilders.nestedQuery(objectPath, query, ScoreMode.None);
        }
        return isNullExpression.isMatchNull() ? new BoolQueryBuilder().mustNot(query) : query;
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
