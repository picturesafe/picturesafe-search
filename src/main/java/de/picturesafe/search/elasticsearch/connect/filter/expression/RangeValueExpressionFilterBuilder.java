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
import de.picturesafe.search.elasticsearch.timezone.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.RangeValueExpression;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.Date;

public class RangeValueExpressionFilterBuilder extends AbstractExpressionFilterBuilder implements TimeZoneAware {

    private String timeZone;

    public RangeValueExpressionFilterBuilder(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    protected boolean supportsExpression(Expression expression) {
        return expression instanceof RangeValueExpression;
    }

    @Override
    protected QueryBuilder buildExpressionFilter(ExpressionFilterBuilderContext context) {
        final MappingConfiguration mappingConfiguration = context.getMappingConfiguration();
        final RangeValueExpression expression = (RangeValueExpression) context.getExpression();
        final String fieldName = expression.getName();
        final FieldConfiguration fieldConf = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
        if (fieldConf == null) {
            throw new RuntimeException("no field configuration found for field name '" + fieldName + "'");
        }

        final Object from = expression.getMinValue();
        final Object to = expression.getMaxValue();
        if (from == null && to == null) {
            return null;
        }

        final RangeQueryBuilder rangeFilterBuilder = QueryBuilders.rangeQuery(fieldName);
        if (from != null) {
            rangeFilterBuilder.from(convert(from));
        }
        if (to != null) {
            rangeFilterBuilder.to(convert(to));
        }

        return rangeFilterBuilder;
    }

    private Object convert(Object value) {
        if (value instanceof Date) {
            return ElasticDateUtils.formatIso((Date) value, timeZone);
        } else {
            return value;
        }
    }
}
