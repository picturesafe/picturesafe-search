/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public final class AliasCreateException extends ElasticsearchException {

    public AliasCreateException(String message) {
        super(message);
    }

    public AliasCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public AliasCreateException(String alias, String indexName, Throwable cause) {
        super("Failed to create alias '" + alias + "' for elasticsearch index '" + indexName + "'!", cause);
    }
}
