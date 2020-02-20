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

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Representation of an aggregation field
 */
public class AggregationField {

    private String name;
    private int maxCount;

    /**
     * Constructor
     * @param name Field name
     * @param maxCount Maximum count of aggregation items
     */
    public AggregationField(String name, int maxCount) {
        this.name = name;
        this.maxCount = maxCount;
    }

    /**
     * Gets the name of the aggregation field
     * @return Field name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the aggregation field
     * @param name Field name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the maximum count of aggregation items
     * @return Maximum count of aggregation items
     */
    public int getMaxCount() {
        return maxCount;
    }

    /**
     * Sets the maximum count of aggregation items
     * @param maxCount Maximum count of aggregation items
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("maxCount", maxCount) //--
                .toString();
    }
}
