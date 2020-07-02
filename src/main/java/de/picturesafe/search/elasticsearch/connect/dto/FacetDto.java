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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class FacetDto {

    private final String name;
    private final String fieldName;
    private final long count;
    private final List<FacetEntryDto> facetEntryDtos;

    public FacetDto(String name, String fieldName, long count, List<FacetEntryDto> facetEntryDtos) {
        this.name = name;
        this.fieldName = fieldName;
        this.count = count;
        this.facetEntryDtos = facetEntryDtos;
    }

    public String getName() {
        return name;
    }

    public String getFieldName() {
        return fieldName;
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
            return new EqualsBuilder()
                    .append(name, target.getName())
                    .append(fieldName, target.getName())
                    .append(count, target.getCount())
                    .append(facetEntryDtos, target.getFacetEntryDtos())
                    .isEquals();
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("fieldName", fieldName) //--
                .append("count", count) //--
                .append("facetEntryDtos", facetEntryDtos) //--
                .toString();
    }
}
