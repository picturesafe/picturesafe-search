/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public class QuerySyntaxException extends ElasticsearchException {

    private final String invalidQueryString;

    public QuerySyntaxException(String message, String invalidQueryString, Throwable cause) {
        super(message, cause);
        this.invalidQueryString = invalidQueryString;
    }

    public String getInvalidQueryString() {
        return invalidQueryString;
    }
}