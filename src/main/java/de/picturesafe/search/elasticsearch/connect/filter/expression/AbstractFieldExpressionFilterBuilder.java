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
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.FieldExpression;

public abstract class AbstractFieldExpressionFilterBuilder implements ExpressionFilterBuilder {

    boolean hasFieldConfiguration(ExpressionFilterBuilderContext context) {
        if (context.getExpression() instanceof FieldExpression) {
            final FieldExpression fieldExpression = (FieldExpression) context.getExpression();

            final String fieldName = fieldExpression.getName();
            if (fieldName.equals(FieldConfiguration.FIELD_NAME_FULLTEXT)) {
                return true;
            }

            return FieldConfiguration.FIELD_NAME_FULLTEXT.equalsIgnoreCase(fieldExpression.getName())
                    || FieldConfigurationUtils.fieldConfiguration(context.getMappingConfiguration(), fieldExpression.getName()) != null;
        } else {
            return false;
        }
    }
}
