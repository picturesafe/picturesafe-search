/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class FacetDto {
    private final String name;
    private final long count;
    private final List<FacetEntryDto> facetEntryDtos;

    public FacetDto(String name, long count, List<FacetEntryDto> facetEntryDtos) {
        this.name = name;
        this.count = count;
        this.facetEntryDtos = facetEntryDtos;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public List<FacetEntryDto> getFacetEntryDtos() {
        return facetEntryDtos;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FacetDto)) {
            return false;
        } else {
            final FacetDto target = (FacetDto) o;
            return new EqualsBuilder().append(count, target.getCount()).append(name, target.getName()).append(facetEntryDtos,
                    target.getFacetEntryDtos()).isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(2621, 4271).append(name).append(count).append(facetEntryDtos).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("count", count) //--
                .append("facetEntryDtos", facetEntryDtos) //--
                .toString();
    }
}
