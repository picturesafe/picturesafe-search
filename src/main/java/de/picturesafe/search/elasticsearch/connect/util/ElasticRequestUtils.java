/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import org.elasticsearch.action.support.WriteRequest;

public class ElasticRequestUtils {

    private ElasticRequestUtils() {
    }

    public static WriteRequest.RefreshPolicy getRefreshPolicy(boolean applyIndexRefresh) {
        WriteRequest.RefreshPolicy refreshPolicy = WriteRequest.RefreshPolicy.NONE;
        if (applyIndexRefresh) {
            refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE;
        }
        return refreshPolicy;
    }
}
