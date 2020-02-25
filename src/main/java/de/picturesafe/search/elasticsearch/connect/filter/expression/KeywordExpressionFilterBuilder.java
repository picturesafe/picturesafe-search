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

public class KeywordExpressionFilterBuilder implements ExpressionFilterBuilder {

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
}
