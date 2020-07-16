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
 * Search result inner hits option
 */
public class InnerHitsOption {

    public static final int DEFAULT_SIZE = 5;

    private final String name;

    private int size = DEFAULT_SIZE;
    private List<SortOption> sortOptions;
    private CollapseOption collapseOption;
    private int from;

    private InnerHitsOption(String name) {
        this.name = name;
    }

    /**
     * Creates an inner hits option with the given name.
     *
     * @param name Name of the inner hits
     * @return Inner hits option
     */
    public static InnerHitsOption name(String name) {
        return new InnerHitsOption(name);
    }

    /**
     * Gets the name of the inner hits.
     *
     * @return Name of the inner hits
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the size of the inner hits.
     *
     * @param size Size of the inner hits
     * @return Inner hits option
     */
    public InnerHitsOption size(int size) {
        this.size = size;
        return this;
    }

    /**
     * Gets the size of the inner hits.
     *
     * @return Size of the inner hits
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets sort options for the inner hits.
     *
     * @param sortOptions Sort options for the inner hits.
     * @return Inner hits option
     */
    public InnerHitsOption sortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
        return this;
    }

    /**
     * Sets sort options for the inner hits.
     *
     * @param sortOptions Sort options for the inner hits.
     * @return Inner hits option
     */
    public InnerHitsOption sortOptions(SortOption... sortOptions) {
        return sortOptions(Arrays.asList(sortOptions));
    }

    /**
     * Gets sort options for the inner hits.
     *
     * @return Sort options for the inner hits
     */
    public List<SortOption> getSortOptions() {
        return sortOptions;
    }

    /**
     * Sets collapse options for the inner hits.
     *
     * @param collapseOption Collapse options for the inner hits
     * @return Inner hits option
     */
    public InnerHitsOption collapseOption(CollapseOption collapseOption) {
        this.collapseOption = collapseOption;
        return this;
    }

    /**
     * Gets collapse options for the inner hits.
     *
     * @return Collapse options for the inner hits
     */
    public CollapseOption getCollapseOption() {
        return collapseOption;
    }

    /**
     * Sets the offset of the first hit to fetch.
     *
     * @param from Offset of the first hit to fetch
     * @return Inner hits option
     */
    public InnerHitsOption from(int from) {
        this.from = from;
        return this;
    }

    /**
     * Gets the offset of the first hit to fetch:
     *
     * @return Offset of the first hit to fetch
     */
    public int getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("size", size) //--
                .append("sortOptions", sortOptions) //--
                .append("collapseOption", collapseOption) //--
                .append("from", from) //--
                .toString();
    }
}
