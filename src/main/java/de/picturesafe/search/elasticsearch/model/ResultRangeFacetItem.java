/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.elasticsearch.api.RangeFacet;

import java.util.Objects;

public class ResultRangeFacetItem extends ResultFacetItem implements RangeFacet {
    private final String from;
    private final String to;

    /**
     * Constructor
     *
     * @param value Value of the item
     * @param count Count of documents
     * @param from  beginning of the interval, can be null for open interval
     * @param to    end of the interval, can be null for open interval
     */
    public ResultRangeFacetItem(Object value, long count, String from, String to) {
        super(value, count);
        this.from = from;
        this.to = to;
    }

    /**
     * Constructor for {@link RangeFacet}
     *
     * @param facet {@link RangeFacet}
     */
    public ResultRangeFacetItem(RangeFacet facet) {
        this(facet.getValue(), facet.getCount(), facet.getFrom(), facet.getTo());
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ResultRangeFacetItem that = (ResultRangeFacetItem) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to);
    }

    @Override
    public String toString() {
        return "ResultRangeFacetItem{"
                + "from='" + from + '\''
                + ", to='" + to + '\''
                + "} " + super.toString();
    }
}
