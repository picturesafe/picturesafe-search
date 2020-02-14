/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.api;

/**
 * Interface for ranged facet.
 */
public interface RangeFacet extends Facet {
    /**
     * Beginning of the range.
     * <p>
     * Can be {@code null} which means open (unbounded) interval, i.e. infinity.
     *
     * @return beginning of closed interval or {@code null} for open interval.
     */
    String getFrom();

    /**
     * End of the range.
     * <p>
     * Can be {@code null} which means open (unbounded) interval, i.e. infinity.
     *
     * @return end of closed interval or {@code null} for open interval.
     */
    String getTo();
}
