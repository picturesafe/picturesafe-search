/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

public class NullAwareAndFilterBuilder {

    private final List<QueryBuilder> queryBuilders = new ArrayList<>();

    public void add(QueryBuilder filterBuilder) {
        if (filterBuilder != null) {
            queryBuilders.add(filterBuilder);
        }
    }

    public QueryBuilder toFilterBuilder() {
        if (queryBuilders.size() == 0) {
            return null;
        } else if (queryBuilders.size() == 1) {
            return queryBuilders.get(0);
        } else {
            final BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolFilter.filter(queryBuilder);
            }

            return boolFilter;
        }
    }

}
