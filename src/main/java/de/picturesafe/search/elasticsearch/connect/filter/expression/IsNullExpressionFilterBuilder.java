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
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.IsNullExpression;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static de.picturesafe.search.elasticsearch.connect.util.QueryBuilderUtils.applyBoost;

public class IsNullExpressionFilterBuilder extends AbstractExpressionFilterBuilder {

    @Override
    protected boolean supportsExpression(Expression expression) {
        return expression instanceof IsNullExpression;
    }

    @Override
    protected QueryBuilder buildExpressionFilter(ExpressionFilterBuilderContext context) {
        final IsNullExpression isNullExpression = (IsNullExpression) context.getExpression();
        final MappingConfiguration mappingConfiguration = context.getMappingConfiguration();
        final String fieldName
                = FieldConfigurationUtils.getElasticFieldName(mappingConfiguration, isNullExpression.getName(), context.getQueryDto().getLocale());
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);

        QueryBuilder query = applyBoost(QueryBuilders.existsQuery(fieldName), isNullExpression);
        if (fieldConfiguration != null && fieldConfiguration.isNestedObject() && !context.isNestedQuery()) {
            final String objectPath = FieldConfigurationUtils.rootFieldName(fieldConfiguration);
            query = QueryBuilders.nestedQuery(objectPath, query, ScoreMode.None);
        }
        return isNullExpression.isMatchNull() ? new BoolQueryBuilder().mustNot(query) : query;
    }
}
