/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import org.elasticsearch.index.query.QueryBuilder;

public interface ExpressionFilterBuilder {

    QueryBuilder buildFilter(ExpressionFilterBuilderContext expressionFilterBuilderContext);

    boolean canHandleSearch(ExpressionFilterBuilderContext expressionFilterBuilderContext);
}
