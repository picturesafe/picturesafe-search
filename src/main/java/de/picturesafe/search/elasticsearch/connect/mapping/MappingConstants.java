/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.mapping;

public class MappingConstants {

    public static final String KEYWORD_FIELD = "keyword";
    public static final String MULTILINGUAL_KEYWORD_FIELD = KEYWORD_FIELD + "_icu";

    private MappingConstants() {
    }
}
