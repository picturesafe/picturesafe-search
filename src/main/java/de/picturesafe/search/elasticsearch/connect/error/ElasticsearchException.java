/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public class ElasticsearchException extends RuntimeException {

    public ElasticsearchException(String message) {
        super(message);
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticsearchException(Throwable cause) {
        super(cause);
    }
}
