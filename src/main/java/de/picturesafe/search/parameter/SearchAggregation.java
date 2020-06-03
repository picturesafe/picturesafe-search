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

package de.picturesafe.search.parameter;

/**
 * Definition of an search aggregation
 *
 * @param <A> Type of the aggregation
 */
public interface SearchAggregation<A extends SearchAggregation<A>> {

    /**
     * Get the aggregation field:
     *
     * @return Name of the aggregation field
     */
    String getField();

    /**
     * Sets the name of the aggregation.
     *
     * @param name  Name of the aggregation
     * @return      The aggregation
     */
    A name(String name);

    /**
     * Gets the name of the aggregation.
     *
     * @return Name of the aggregation
     */
    String getName();
}
