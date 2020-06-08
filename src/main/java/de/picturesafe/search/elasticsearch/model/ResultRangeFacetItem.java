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

import de.picturesafe.search.elasticsearch.api.RangeFacetItem;

import java.util.Objects;

public class ResultRangeFacetItem extends ResultFacetItem implements RangeFacetItem {
    private final String from;
    private final String to;

    /**
     * Constructor
     *
     * @param value Value of the item
     * @param count Count of documents
     * @param from  beginning of the interval, can be null for open interval
     * @param to    end of the interval, can be null for open interval
     */
    public ResultRangeFacetItem(Object value, long count, String from, String to) {
        super(value, count);
        this.from = from;
        this.to = to;
    }

    /**
     * Constructor for {@link RangeFacetItem}
     *
     * @param facet {@link RangeFacetItem}
     */
    public ResultRangeFacetItem(RangeFacetItem facet) {
        this(facet.getValue(), facet.getCount(), facet.getFrom(), facet.getTo());
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ResultRangeFacetItem that = (ResultRangeFacetItem) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to);
    }

    @Override
    public String toString() {
        return "ResultRangeFacetItem{"
                + "from='" + from + '\''
                + ", to='" + to + '\''
                + "} " + super.toString();
    }
}
