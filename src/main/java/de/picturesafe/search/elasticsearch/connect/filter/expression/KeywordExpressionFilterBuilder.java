/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.KeywordExpression;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;

import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.keywordFieldName;

public class KeywordExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder {

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof KeywordExpression)) {
            return null;
        }

        final KeywordExpression keywordExpression = (KeywordExpression) context.getExpression();
        final Object value = keywordExpression.getValue();
        if (!(value instanceof String) || StringUtils.isEmpty((String) value)) {
            return null;
        }

        final MappingConfiguration mappingConfig = context.getMappingConfiguration();
        final FieldConfiguration fieldConfig = FieldConfigurationUtils.fieldConfiguration(mappingConfig, keywordExpression.getName());

        String fieldName
                = FieldConfigurationUtils
                .getElasticFieldName(context.getMappingConfiguration(), keywordExpression.getName(), context.getQueryDto().getLocale());
        fieldName = keywordFieldName(fieldConfig, fieldName);

        final TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(fieldName, value);
        switch (keywordExpression.getComparison()) {
            case EQ:
                return termsQueryBuilder;
            case NOT_EQ:
                return QueryBuilders.boolQuery().mustNot(termsQueryBuilder);
            default:
                throw new RuntimeException("Unsupported comparison " + keywordExpression.getComparison());
        }
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext context) {
        if (context.getExpression() instanceof KeywordExpression) {
            return hasFieldConfiguration(context);
        } else {
            return false;
        }
    }
}
