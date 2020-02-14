/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

public class ElasticExceptionCause {

    public enum Type {
        COMMON,
        QUERY_SYNTAX
    }

    public ElasticExceptionCause(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    private Type type;
    private String message;

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
