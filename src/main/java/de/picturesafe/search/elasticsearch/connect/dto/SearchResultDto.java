/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class SearchResultDto {

    private final long totalHitCount;
    private final boolean exactCount;
    private final List<SearchHitDto> hits;
    private final List<FacetDto> facetDtoList;

    public SearchResultDto(long totalHitCount, boolean exactCount, List<SearchHitDto> hits, List<FacetDto> facetDtoList) {
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

    public List<SearchHitDto> getHits() {
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
        if (!(o instanceof SearchResultDto)) {
            return false;
        } else {
            final SearchResultDto that = (SearchResultDto) o;
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
