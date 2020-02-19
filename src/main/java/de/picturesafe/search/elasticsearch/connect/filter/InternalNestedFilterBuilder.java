/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilderContext;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InternalNestedFilterBuilder implements InternalFilterBuilder {

    @Override
    public QueryBuilder build(String key, Object value, ExpressionFilterBuilderContext context) {
        if (isSortFilter(context)) {
            // Build filter for nested sort
            if (value instanceof Object[]) {
                final Object[] values = (Object[]) value;
                if (values.length > 1) {
                    return QueryBuilders.termsQuery(key, values);
                } else {
                    return QueryBuilders.termQuery(key, values[0]);
                }
            } else {
                return QueryBuilders.termQuery(key, value);
            }
        } else {
            // Expressions on nested fields must be built as query because otherwise the score is missing!
            return null;
        }
    }

    private boolean isSortFilter(ExpressionFilterBuilderContext context) {
        final QueryDto queryDto = context.getQueryDto();
        return queryDto.getQueryRangeDto() == null || queryDto.getQueryRangeDto().getStart() < 0;
    }
}
