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

import java.util.Arrays;
import java.util.List;

/**
 * Search result collapse option
 */
public class CollapseOption {

    private final String field;
    private List<InnerHitsOption> innerHitsOptions;

    private CollapseOption(String field) {
        this.field = field;
    }

    /**
     * Creates a collapse option for the given field.
     *
     * @param field Field name
     * @return Collapse option
     */
    public static CollapseOption field(String field) {
        return new CollapseOption(field);
    }

    /**
     * Gets the field to collapse the search result on.
     *
     * @return Field name
     */
    public String getField() {
        return field;
    }

    /**
     * Sets the inner hits to collapse the search result to.
     *
     * @param innerHitsOptions Inner hits to collapse the search result to
     * @return Collapse option
     */
    public CollapseOption innerHits(List<InnerHitsOption> innerHitsOptions) {
        this.innerHitsOptions = innerHitsOptions;
        return this;
    }

    /**
     * Sets the inner hits to collapse the search result to.
     *
     * @param innerHitsOptions Inner hits to collapse the search result to
     * @return Collapse option
     */
    public CollapseOption innerHits(InnerHitsOption... innerHitsOptions) {
        return innerHits(Arrays.asList(innerHitsOptions));
    }

    /**
     * Gets the inner hits to collapse the search result to.
     *
     * @return Inner hits
     */
    public List<InnerHitsOption> getInnerHitsOptions() {
        return innerHitsOptions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("field", field) //--
                .append("innerHitsOptions", innerHitsOptions) //--
                .toString();
    }
}
