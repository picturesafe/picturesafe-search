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

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Facet of an elasticsearch result
 */
public class ResultFacet {

    private final String name;
    private final String fieldName;
    private final long count;
    private final List<ResultFacetItem> facetItems;

    /**
     * Constructor
     *
     * @param name          Name of the facet
     * @param fieldName     Name of the aggregation field (may be null)
     * @param count         Total count of documents
     * @param facetItems    Facet items
     */
    public ResultFacet(String name, String fieldName, long count, List<ResultFacetItem> facetItems) {
        this.name = name;
        this.fieldName = fieldName;
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
     * Gets the name of the aggregation field.
     *
     * @return Name of the aggregation field (may be null)
     */
    public String getFieldName() {
        return fieldName;
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
                .append(name, that.name)
                .append(fieldName, that.fieldName)
                .append(count, that.count)
                .append(facetItems, that.facetItems)
                .isEquals();
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
                .append("facetItems", facetItems) //--
                .toString();
    }
}
