/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientDeleteAction extends AbstractRestClientAsyncAction<DeleteRequest, DeleteResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, DeleteRequest deleteRequest) {
        client.deleteAsync(deleteRequest, RequestOptions.DEFAULT, this);
    }
}
