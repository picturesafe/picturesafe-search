/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import java.util.Locale;

public enum ElasticsearchType {

    TEXT,
    LONG,
    INTEGER,
    SHORT,
    BYTE,
    DOUBLE,
    FLOAT,
    DATE,
    BOOLEAN,
    NESTED,
    COMPLETION;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
