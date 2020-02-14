/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.impl;

public enum MissingValueSortPosition {
    FIRST("first"), LAST("last");

    private final String value;

    MissingValueSortPosition(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
