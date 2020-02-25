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
import de.picturesafe.search.expression.InExpression;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InExpressionFilterBuilder implements ExpressionFilterBuilder {

    private QueryBuilder inFilter(final String fieldName, final Object[] values, ExpressionFilterBuilderContext filterBuilderContext) {
        Validate.notEmpty(fieldName, "Parameter 'fieldName' may be not empty!");
        if (ArrayUtils.isEmpty(values)) {
            return null;
        }

        final MappingConfiguration mappingConfiguration = filterBuilderContext.getMappingConfiguration();
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
        final String queryFieldName = FieldConfigurationUtils.keywordFieldName(fieldConfiguration, fieldName);

        QueryBuilder queryBuilder;
        if (values.length > 1) {
            queryBuilder = QueryBuilders.termsQuery(queryFieldName, values);
        } else {
            queryBuilder = QueryBuilders.termQuery(queryFieldName, values[0]);
        }

        if (fieldConfiguration != null && fieldConfiguration.isNestedObject()) {
            final String objectPath = FieldConfigurationUtils.rootFieldName(fieldConfiguration);
            queryBuilder = QueryBuilders.nestedQuery(objectPath, queryBuilder, ScoreMode.None);
        }

        return queryBuilder;
    }

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext) {
        if (!(expressionFilterBuilderContext.getExpression() instanceof InExpression)) {
            return null;
        }

        final InExpression inExpression = (InExpression) expressionFilterBuilderContext.getExpression();
        return inFilter(inExpression.getName(), inExpression.getValues(), expressionFilterBuilderContext);
    }
}
