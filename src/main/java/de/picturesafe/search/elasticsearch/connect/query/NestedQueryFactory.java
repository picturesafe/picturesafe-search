/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

@Component
public class NestedQueryFactory implements QueryFactory {

    @Override
    public boolean supports(Expression parameter) {
        return parameter instanceof ValueExpression && !((ValueExpression) parameter).getName().equals(FieldConfiguration.FIELD_NAME_FULLTEXT);
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, MappingConfiguration mappingConfiguration, Expression parameter) {
        final ValueExpression valueExpression = (ValueExpression) parameter;
        final String fieldName = valueExpression.getName();
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
        if (fieldConfiguration != null && fieldConfiguration.isNestedObject()) {
            final QueryBuilder targetFilterBuilder = QueryBuilders.termQuery(fieldName, valueExpression.getValue());
            return QueryBuilders.nestedQuery(fieldConfiguration.getName(), QueryBuilders.boolQuery().filter(targetFilterBuilder), ScoreMode.Total);
        }

        return null;
    }
}
