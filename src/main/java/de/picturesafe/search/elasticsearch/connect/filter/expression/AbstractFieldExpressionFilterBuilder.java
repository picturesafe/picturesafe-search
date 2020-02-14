/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
