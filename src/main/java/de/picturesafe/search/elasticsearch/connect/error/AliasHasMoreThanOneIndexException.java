/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public class AliasHasMoreThanOneIndexException extends ElasticsearchException {

    public AliasHasMoreThanOneIndexException(String message) {
        super(message);
    }

}
