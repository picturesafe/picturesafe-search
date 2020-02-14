/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Facet of an elasticsearch result
 */
public class ResultFacet {

    private final String name;
    private final long count;
    private final List<ResultFacetItem> facetItems;

    /**
     * Constructor
     *
     * @param name          Name of the facet
     * @param count         Total count of documents
     * @param facetItems    Facet items
     */
    public ResultFacet(String name, long count, List<ResultFacetItem> facetItems) {
        this.name = name;
        this.count = count;
        this.facetItems = facetItems;
    }

    /**
     * Gets the name of the facet.
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the total count of documents.
     *
     * @return Count
     */
    public long getCount() {
        return count;
    }

    /**
     * Gets the facet items.
     *
     * @return Facet items
     */
    public List<ResultFacetItem> getFacetItems() {
        return facetItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResultFacet)) {
            return false;
        }

        final ResultFacet that = (ResultFacet) o;
        return new EqualsBuilder()
                .append(count, that.count)
                .append(name, that.name)
                .append(facetItems, that.facetItems)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("count", count) //--
                .append("facetItems", facetItems) //--
                .toString();
    }
}
