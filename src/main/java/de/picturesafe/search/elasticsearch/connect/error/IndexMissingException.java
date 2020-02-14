/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.error;

/**
 * Thrown to hide the elasticsearch internal IndexMissingException, if you access an index that does not exist.
 */
public class IndexMissingException extends ElasticsearchException {
    public IndexMissingException(String s) {
        super("missing index " + s);
    }
}
