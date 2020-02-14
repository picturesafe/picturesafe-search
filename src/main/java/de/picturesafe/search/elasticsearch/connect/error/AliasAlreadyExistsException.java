/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public class AliasAlreadyExistsException extends ElasticsearchException {

    public AliasAlreadyExistsException(String reason) {
        super(reason);
    }

    public AliasAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AliasAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
