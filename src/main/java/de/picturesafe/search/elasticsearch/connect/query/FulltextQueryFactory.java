/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.PhraseMatchHelper;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.ValueExpression;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_EQ;

@Component
public class FulltextQueryFactory implements QueryFactory {

    private QueryConfiguration queryConfig;

    @Autowired
    public FulltextQueryFactory(QueryConfiguration queryConfig) {
        this.queryConfig = queryConfig;
    }

    @Override
    public boolean supports(Expression parameter) {
        return parameter instanceof FulltextExpression
                || (parameter instanceof ValueExpression && ((ValueExpression) parameter).getName().equals(FieldConfiguration.FIELD_NAME_FULLTEXT));
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, MappingConfiguration mappingConfiguration, Expression parameter) {
        final ValueExpression valueExpression = (ValueExpression) parameter;
        final String value = valueExpression.getValue().toString();
        if (!StringUtils.isBlank(value)) {
            QueryStringQueryBuilder result = QueryBuilders.queryStringQuery(adjustPhrase(value))
                    .field(FieldConfiguration.FIELD_NAME_FULLTEXT)
                    .defaultOperator(queryConfig.getDefaultQueryStringOperator());
            if (value.contains("*") || value.contains("?")) {
                result = result.analyzeWildcard(true);
            }

            if (valueExpression.getComparison() != null && valueExpression.getComparison().equals(NOT_EQ)) {
                return QueryBuilders.boolQuery().mustNot(result);
            } else {
                return result;
            }
        }
        return null;
    }

    private String adjustPhrase(String value) {
        if (PhraseMatchHelper.matchPhrase(value)) {
            return PhraseMatchHelper.replacePhraseMatchChars(value);
        }
        return value;
    }
}
