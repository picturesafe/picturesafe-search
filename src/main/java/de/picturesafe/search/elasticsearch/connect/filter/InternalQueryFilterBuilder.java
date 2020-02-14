/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class InternalQueryFilterBuilder implements InternalFilterBuilder {

    private final QueryConfiguration queryConfig;

    public InternalQueryFilterBuilder(QueryConfiguration queryConfig) {
        this.queryConfig = queryConfig;
    }

    @Override
    public QueryBuilder build(String key, Object value) {
        return QueryBuilders.boolQuery().filter(
                QueryBuilders.queryStringQuery(convertObject(value)).field(key)
                        .defaultOperator(queryConfig.getDefaultQueryStringOperator())
                        .analyzeWildcard(true));
    }

    private static String convertObject(Object value) {
        final String result;

        if (value == null) {
            result = null;
        } else if (value instanceof Number) {
            result = value.toString();
        } else if (value instanceof String) {
            result = (String) value;
        } else {
            throw new IllegalArgumentException("Can't handle value of class " + value.getClass().getName());
        }
        return result;
    }
}
