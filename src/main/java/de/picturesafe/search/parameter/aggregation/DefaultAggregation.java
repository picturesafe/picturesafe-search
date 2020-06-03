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

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Default aggregation
 * The actual type of the aggregation will be determined by the field type, and default parameters will be used.
 */
public class DefaultAggregation extends AbstractAggregation<DefaultAggregation> {

    /**
     * Creates a default aggregation for the given field.
     *
     * @param field Name of the field
     * @return      Aggregation
     */
    public static DefaultAggregation field(String field) {
        final DefaultAggregation aggregation = new DefaultAggregation();
        aggregation.field = field;
        return aggregation;
    }

    @Override
    protected DefaultAggregation self() {
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .appendSuper(super.toString()) //--
                .toString();
    }
}
