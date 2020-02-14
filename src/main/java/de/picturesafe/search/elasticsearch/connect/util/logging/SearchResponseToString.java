/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util.logging;

import org.elasticsearch.action.search.SearchResponse;

public class SearchResponseToString {

    private final SearchResponse searchResponse;

    public SearchResponseToString(SearchResponse searchResponse) {
        this.searchResponse = searchResponse;
    }

    @Override
    public String toString() {
        return (searchResponse != null) ? searchResponse.toString() : null;
    }
}
