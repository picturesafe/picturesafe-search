/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientSearchAction extends AbstractRestClientAsyncAction<SearchRequest, SearchResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, SearchRequest searchRequest) {
        client.searchAsync(searchRequest, RequestOptions.DEFAULT, this);
    }
}
