/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.connect.filter.expression.ExpressionFilterBuilderContext;
import org.elasticsearch.index.query.QueryBuilder;

public interface InternalFilterBuilder {
    QueryBuilder build(String key, Object value, ExpressionFilterBuilderContext context);
}
