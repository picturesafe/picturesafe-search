/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.RangeValueExpression;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.Date;

public class RangeValueExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder implements TimeZoneAware {

    private String timeZone;

    public RangeValueExpressionFilterBuilder(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof RangeValueExpression)) {
            return null;
        }
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

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext context) {
        if (context.getExpression() instanceof RangeValueExpression) {
            return hasFieldConfiguration(context);
        } else {
            return false;
        }
    }
}
