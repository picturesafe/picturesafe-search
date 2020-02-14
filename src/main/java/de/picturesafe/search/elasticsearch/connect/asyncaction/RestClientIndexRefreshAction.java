/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

public class RestClientIndexRefreshAction extends AbstractRestClientAsyncAction<RefreshRequest, RefreshResponse> {

    @Override
    public void asyncAction(RestHighLevelClient client, RefreshRequest refreshRequest) {
        client.indices().refreshAsync(refreshRequest, RequestOptions.DEFAULT, this);
    }
}
