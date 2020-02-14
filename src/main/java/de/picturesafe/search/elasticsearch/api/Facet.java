/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.api;

/**
 * Interface for result facet
 */
public interface Facet {
    /**
     * Gets the value of the item.
     *
     * @return Value
     */
    Object getValue();

    /**
     * Gets the count of documents.
     *
     * @return Count of documents
     */
    long getCount();
}
