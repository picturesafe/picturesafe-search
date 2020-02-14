/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.InExpression;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder {

    private QueryBuilder inFilter(final String fieldName, final Object[] values, ExpressionFilterBuilderContext filterBuilderContext) {
        Validate.notEmpty(fieldName, "Parameter 'fieldName' may be not empty!");

        final MappingConfiguration mappingConfiguration = filterBuilderContext.getMappingConfiguration();
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
        final String queryFieldName = FieldConfigurationUtils.keywordFieldName(fieldConfiguration, fieldName);

        final QueryBuilder queryBuilder;
        // Create FilterBuilder for all passed values
        if (ArrayUtils.isEmpty(values)) {
            final QueryBuilder query;
            if (fieldConfiguration != null && fieldConfiguration.isNestedObject()) {
                query = QueryBuilders.nestedQuery(queryFieldName, QueryBuilders.matchAllQuery(), ScoreMode.None);
            } else {
                query = QueryBuilders.existsQuery(queryFieldName);
            }
            queryBuilder = QueryBuilders.boolQuery().mustNot(query);
        } else if (values.length > 1) {
            // Create InFilter for array of values
            queryBuilder = QueryBuilders.termsQuery(queryFieldName, values);
        } else {
            // Create TermFilter for scalar values
            queryBuilder = QueryBuilders.termQuery(queryFieldName, values[0]);
        }

        return queryBuilder;
    }

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        if (!(expressionFilterBuilderContext.getExpression() instanceof InExpression)) {
            return null;
        }

        final InExpression inExpression = (InExpression) expressionFilterBuilderContext.getExpression();
        final Object[] values = inExpression.getValues();
        if (values == null || values.length > 0) {
            return inFilter(inExpression.getName(), values, expressionFilterBuilderContext);
        }

        return null;
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext context) {
        if (context.getExpression() instanceof InExpression) {
            return hasFieldConfiguration(context);
        } else {
            return false;
        }
    }
}
