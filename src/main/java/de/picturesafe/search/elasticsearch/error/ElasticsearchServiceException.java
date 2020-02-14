/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.error;

public class ElasticsearchServiceException extends RuntimeException {

    public ElasticsearchServiceException(String message) {
        super(message);
    }

    public ElasticsearchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
