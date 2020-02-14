/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util.logging;

import org.elasticsearch.search.builder.SearchSourceBuilder;

public class SearchSourceBuilderToString {

    private final SearchSourceBuilder searchSourceBuilder;

    public SearchSourceBuilderToString(SearchSourceBuilder searchSourceBuilder) {
        this.searchSourceBuilder = searchSourceBuilder;
    }

    @Override
    public String toString() {
        return (searchSourceBuilder != null) ? searchSourceBuilder.toString() : null;
    }
}
