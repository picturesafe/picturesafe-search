/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Transports data for a filter for a search query.
 */
public class QueryFilterDto {
    private final String key;
    private final Object value;

    public QueryFilterDto(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(163, 139).append(key).append(value).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryFilterDto)) {
            return false;
        } else {
            final QueryFilterDto target = (QueryFilterDto) o;
            return new EqualsBuilder().append(key, target.getKey()).append(value, target.getValue()).isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("key", key) //--
                .append("value", value) //--
                .toString();
    }
}
