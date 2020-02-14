/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientIndexAction extends AbstractRestClientAsyncAction<IndexRequest, IndexResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, IndexRequest indexRequest) {
        client.indexAsync(indexRequest, RequestOptions.DEFAULT, this);
    }
}
