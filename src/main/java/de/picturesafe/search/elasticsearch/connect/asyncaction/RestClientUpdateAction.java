/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientUpdateAction extends AbstractRestClientAsyncAction<UpdateRequest, UpdateResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, UpdateRequest updateRequest) {
        client.updateAsync(updateRequest, RequestOptions.DEFAULT, this);
    }
}
