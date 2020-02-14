/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import org.elasticsearch.index.query.QueryBuilder;

public class InternalNestedFilterBuilder implements InternalFilterBuilder {

    @Override
    public QueryBuilder build(String key, Object value) {
        return null;
    }
}
