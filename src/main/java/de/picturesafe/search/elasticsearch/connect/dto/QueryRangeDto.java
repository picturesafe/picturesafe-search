/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Transports the range to be selected for a search query (paging).
 */
public class QueryRangeDto {

    private final int start;
    private final int limit;
    private Long maxTrackTotalHits;

    public QueryRangeDto(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    public QueryRangeDto(int start, int limit, Long maxTrackTotalHits) {
        this(start, limit);
        this.maxTrackTotalHits = maxTrackTotalHits;
    }

    public int getStart() {
        return start;
    }

    public int getLimit() {
        return limit;
    }

    public Long getMaxTrackTotalHits() {
        return maxTrackTotalHits;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(start)
                .append(limit)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryRangeDto)) {
            return false;
        } else {
            final QueryRangeDto that = (QueryRangeDto) o;
            return new EqualsBuilder()
                    .append(start, that.start)
                    .append(limit, that.limit)
                    .append(maxTrackTotalHits, that.maxTrackTotalHits)
                    .isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("start", start) //--
                .append("limit", limit) //--
                .append("maxTrackTotalHits", maxTrackTotalHits) //--
                .toString();
    }
}
