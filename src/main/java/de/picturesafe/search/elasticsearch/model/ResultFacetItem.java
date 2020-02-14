/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.elasticsearch.api.Facet;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Item of a result facet
 */
public class ResultFacetItem implements Facet {

    private final Object value;
    private final long count;

    /**
     * Constructor
     *
     * @param value Value of the item
     * @param count Count of documents
     */
    public ResultFacetItem(Object value, long count) {
        this.value = value;
        this.count = count;
    }

    public ResultFacetItem(Facet facet) {
        this(facet.getValue(), facet.getCount());
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultFacetItem)) {
            return false;
        }

        final ResultFacetItem that = (ResultFacetItem) o;
        return new EqualsBuilder()
                .append(count, that.count)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("value", value) //--
                .append("count", count) //--
                .toString();
    }
}
