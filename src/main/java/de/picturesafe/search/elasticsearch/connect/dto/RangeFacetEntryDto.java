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

import de.picturesafe.search.elasticsearch.api.RangeFacetItem;
import org.elasticsearch.search.aggregations.bucket.range.Range;

import java.util.Objects;

public class RangeFacetEntryDto extends FacetEntryDto implements RangeFacetItem {

    private final String from;
    private final String to;

    public RangeFacetEntryDto(String key, long count, String from, String to) {
        super(key, count);
        this.from = from;
        this.to = to;
    }

    public RangeFacetEntryDto(Range.Bucket bucket) {
        this(bucket.getKeyAsString(), bucket.getDocCount(), bucket.getFromAsString(), bucket.getToAsString());
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
        final RangeFacetEntryDto that = (RangeFacetEntryDto) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), from, to);
    }

    @Override
    public String toString() {
        return "RangeFacetEntryDto{from='" + from + '\'' + ", to='" + to + '\'' + "} " + super.toString();
    }
}
