/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import org.elasticsearch.index.query.QueryBuilder;

public interface InternalFilterBuilder {
    QueryBuilder build(String key, Object value);
}
