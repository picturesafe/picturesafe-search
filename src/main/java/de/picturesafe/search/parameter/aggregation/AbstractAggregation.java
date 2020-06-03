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

package de.picturesafe.search.parameter.aggregation;

import de.picturesafe.search.parameter.SearchAggregation;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Abstract implementation of an aggregation definition
 *
 * @param <A> Type of the aggregation
 */
public abstract class AbstractAggregation<A extends SearchAggregation<A>> implements SearchAggregation<A> {

    protected String field;
    protected String name;

    @Override
    public String getField() {
        return field;
    }

    @Override
    public A name(String name) {
        this.name = name;
        return self();
    }

    protected abstract A self();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractAggregation<?> that = (AbstractAggregation<?>) o;
        return new EqualsBuilder()
                .append(field, that.field)
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("field", field) //--
                .append("name", name) //--
                .toString();
    }
}
