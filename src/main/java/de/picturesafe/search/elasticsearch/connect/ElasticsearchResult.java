/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

public class ElasticsearchResult {

    private final long totalHitCount;
    private final boolean exactCount;
    private final List<Map<String, Object>> hits;
    private final List<FacetDto> facetDtoList;

    public ElasticsearchResult(long totalHitCount, boolean exactCount, List<Map<String, Object>> hits, List<FacetDto> facetDtoList) {
        this.totalHitCount = totalHitCount;
        this.exactCount = exactCount;
        this.hits = hits;
        this.facetDtoList = facetDtoList;
    }

    public long getTotalHitCount() {
        return totalHitCount;
    }

    public boolean isExactCount() {
        return exactCount;
    }

    public List<Map<String, Object>> getHits() {
        return hits;
    }

    public List<FacetDto> getFacetDtoList() {
        return facetDtoList;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(totalHitCount)
                .append(hits)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElasticsearchResult)) {
            return false;
        } else {
            final ElasticsearchResult that = (ElasticsearchResult) o;
            return new EqualsBuilder()
                    .append(totalHitCount, that.totalHitCount)
                    .append(exactCount, that.exactCount)
                    .append(hits, that.hits)
                    .append(facetDtoList, that.facetDtoList)
                    .isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("totalHitCount", totalHitCount) //--
                .append("exactCount", exactCount) //--
                .append("hits", hits) //--
                .append("facetDtoList", facetDtoList) //--
                .toString();
    }
}
