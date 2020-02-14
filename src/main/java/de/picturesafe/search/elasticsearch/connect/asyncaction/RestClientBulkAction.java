/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientBulkAction extends AbstractRestClientAsyncAction<BulkRequest, BulkResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, BulkRequest bulkRequest) {
        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, this);
    }
}
