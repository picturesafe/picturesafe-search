/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Transports the data for a facet search query.
 */
public class QueryFacetDto {
    private final String field;
    private final int size;
    private int shardSize;

    public QueryFacetDto(String field, int size, int shardSize) {
        this.field = field;
        this.size = size;
        this.shardSize = shardSize;
    }

    public String getField() {
        return field;
    }

    public int getSize() {
        return size;
    }

    public int getShardSize() {
        return shardSize;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5099, 601).append(field).append(size).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryFacetDto)) {
            return false;
        } else {
            final QueryFacetDto target = (QueryFacetDto) o;
            return new EqualsBuilder().append(field, target.getField()).append(size, target.getSize()).append(shardSize,
                    target.getShardSize()).isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("field", field) //--
                .append("size", size) //--
                .append("shardSize", shardSize) //--
                .toString();
    }
}
