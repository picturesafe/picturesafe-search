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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Additional parameters for search operations
 */
public class SearchParameter {

    public static final SearchParameter DEFAULT = builder().build();

    private final List<SortOption> sortOptions;
    private final Integer maxResults;
    private final Long maxTrackTotalHits;
    private final Integer pageSize;
    private final Integer pageIndex;
    private final boolean permissionCheckEnabled;
    private final boolean ignoreState;
    private final boolean ignoreDeleted;
    private final String language;
    private final List<SearchAggregation<?>> aggregations;
    private final List<String> fieldsToResolve;
    private final boolean optimizeExpressions;

    private SearchParameter(Builder builder) {
        sortOptions = builder.sortOptions;
        maxResults = builder.maxResults;
        maxTrackTotalHits = builder.maxTrackTotalHits;
        pageSize = builder.pageSize;
        pageIndex = builder.pageIndex;
        permissionCheckEnabled = builder.permissionCheckEnabled;
        ignoreState = builder.ignoreState;
        ignoreDeleted = builder.ignoreDeleted;
        language = builder.language;
        aggregations = builder.aggregations;
        fieldsToResolve = builder.fieldsToResolve;
        optimizeExpressions = builder.optimizeExpressions;
    }

    /**
     * Gets the result sort options (in descending priority)
     *
     * @return Result sort options (in descending priority)
     */
    public List<SortOption> getSortOptions() {
        return sortOptions;
    }

    /**
     * Gets the maximum number of results
     *
     * @return Maximum number of results
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Gets the maximum number of total hits to track. If this number is exceeded by the number of current search hits tracking will be stopped and the total
     * hits count will be marked as not exact.
     *
     * @return Maximum number of total hits to track (null = use default value)
     */
    public Long getMaxTrackTotalHits() {
        return maxTrackTotalHits;
    }

    /**
     * Gets the pagination page size
     *
     * @return Pagination page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Gets the pagination page index
     *
     * @return Pagination page index (starts with 1)
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    /**
     * Gets if permission checks are enabled (default=true)
     *
     * @return TRUE if permission checks are enabled (default=true)
     *
     * NOT IMPLEMENTED AT THE MOMENT
     */
    public boolean isPermissionCheckEnabled() {
        return permissionCheckEnabled;
    }

    /**
     * Gets if the state of the record should be ignored (default=false)
     *
     * @return TRUE if the state of the record should be ignored (default=false)
     *
     * NOT IMPLEMENTED AT THE MOMENT
     */
    public boolean isIgnoreState() {
        return ignoreState;
    }

    /**
     * Gets if deleted records (state) should be ignored (default=false)
     *
     * @return TRUE if deleted records (state) should be ignored (default=false)
     *
     * NOT IMPLEMENTED AT THE MOMENT
     */
    public boolean isIgnoreDeleted() {
        return ignoreDeleted;
    }

    /**
     * Gets the search language
     *
     * @return Search language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the aggregations
     *
     * @return List of aggregations
     */
    public List<SearchAggregation<?>> getAggregations() {
        return aggregations;
    }

    /**
     * Gets the names of the fields to be resolved in the search result.
     *
     * @return Names of the fields to be resolved
     */
    public List<String> getFieldsToResolve() {
        return fieldsToResolve;
    }

    /**
     * Checks if expressions should be optimized.
     *
     * @return true if expressions should be optimized
     * @see de.picturesafe.search.expression.Expression#optimize()
     */
    public boolean isOptimizeExpressions() {
        return optimizeExpressions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("sortOptions", sortOptions) //--
                .append("maxResults", maxResults) //--
                .append("maxTrackTotalHits", maxTrackTotalHits) //--
                .append("pageSize", pageSize) //--
                .append("pageIndex", pageIndex) //--
                .append("permissionCheckEnabled", permissionCheckEnabled) //--
                .append("ignoreState", ignoreState) //--
                .append("ignoreDeleted", ignoreDeleted) //--
                .append("language", language) //--
                .append("aggregationFields", aggregations) //--
                .append("fieldsToResolve", fieldsToResolve) //--
                .append("optimizeExpressions", optimizeExpressions) //--
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        List<SortOption> sortOptions = new ArrayList<>();
        Integer maxResults;
        Long maxTrackTotalHits;
        Integer pageSize;
        boolean permissionCheckEnabled = true;
        boolean ignoreState;
        boolean ignoreDeleted = true;
        Integer pageIndex;
        String language;
        List<SearchAggregation<?>> aggregations = new ArrayList<>();
        List<String> fieldsToResolve = new ArrayList<>();
        boolean optimizeExpressions;

        private Builder() {
        }

        /**
         * Sets the result sort options (in descending priority)
         *
         * @param sortOptions Result sort options (in descending priority)
         * @return Builder
         */
        public Builder sortOptions(List<SortOption> sortOptions) {
            this.sortOptions = sortOptions;
            return this;
        }

        /**
         * Sets the result sort options (in descending priority)
         *
         * @param sortOptions Result sort options (in descending priority)
         * @return Builder
         */
        public Builder sortOptions(SortOption... sortOptions) {
            this.sortOptions = Arrays.asList(sortOptions);
            return this;
        }

        /**
         * Adds a result sort option
         *
         * @param sortOption Result sort option to add
         * @return Builder
         */
        public Builder addSortOption(SortOption sortOption) {
            sortOptions.add(sortOption);
            return this;
        }

        /**
         * Sets the maximum number of results
         *
         * @param maxResults Maximum number of results
         * @return Builder
         */
        public Builder maxResults(Integer maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        /**
         * Sets the maximum number of total hits to track. If this number is exceeded by the number of current search hits tracking will be stopped and the
         * total hits count will be marked as not exact.
         *
         * @param maxTrackTotalHits Maximum number of total hits to track (null = use default value)
         * @return Builder
         */
        public Builder maxTrackTotalHits(Long maxTrackTotalHits) {
            this.maxTrackTotalHits = maxTrackTotalHits;
            return this;
        }

        /**
         * Sets the pagination page size
         *
         * @param pageSize Pagination page size
         * @return Builder
         */
        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /**
         * Sets the pagination page index
         *
         * @param pageIndex Pagination page index (starts with 1)
         * @return Builder
         */
        public Builder pageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        /**
         * Sets if permission checks are enabled (default=true)
         *
         * @param permissionCheckEnabled TRUE if permission checks are enabled (default=true)
         * @return Builder
         *
         * NOT IMPLEMENTED AT THE MOMENT
         */
        public Builder permissionCheckEnabled(boolean permissionCheckEnabled) {
            this.permissionCheckEnabled = permissionCheckEnabled;
            return this;
        }

        /**
         * Sets if the state of the record should be ignored (default=false)
         *
         * @param ignoreState TRUE if the state of the record should be ignored (default=false)
         * @return Builder
         *
         * NOT IMPLEMENTED AT THE MOMENT
         */
        public Builder ignoreState(boolean ignoreState) {
            this.ignoreState = ignoreState;
            return this;
        }

        /**
         * Sets if deleted records (state) should be ignored (default=false)
         *
         * @param ignoreDeleted TRUE if deleted records (state) should be ignored (default=false)
         * @return Builder
         *
         * NOT IMPLEMENTED AT THE MOMENT
         */
        public Builder ignoreDeleted(boolean ignoreDeleted) {
            this.ignoreDeleted = ignoreDeleted;
            return this;
        }

        /**
         * Sets the search language
         *
         * @param language Search language
         * @return Builder
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the aggregation fields
         *
         * @param aggregations List of aggregations
         * @return Builder
         */
        public Builder aggregations(List<SearchAggregation<?>> aggregations) {
            this.aggregations = aggregations;
            return this;
        }

        /**
         * Sets the aggregation fields
         *
         * @param aggregations List of aggregations
         * @return Builder
         */
        public Builder aggregations(SearchAggregation<?>... aggregations) {
            this.aggregations = Arrays.asList(aggregations);
            return this;
        }

        /**
         * Sets the names of the fields to be resolved in the search result.
         *
         * @param fieldsToResolve Names of the fields to be resolved
         * @return Builder
         */
        public Builder fieldsToResolve(List<String> fieldsToResolve) {
            this.fieldsToResolve = fieldsToResolve;
            return this;
        }

        /**
         * Sets the names of the fields to be resolved in the search result.
         *
         * @param fieldsToResolve Names of the fields to be resolved
         * @return Builder
         */
        public Builder fieldsToResolve(String... fieldsToResolve) {
            this.fieldsToResolve = Arrays.asList(fieldsToResolve);
            return this;
        }

        /**
         * Sets if expressions should be optimized.
         *
         * @param optimizeExpressions true if expressions should be optimized
         * @see de.picturesafe.search.expression.Expression#optimize()
         * @return Builder
         */
        public Builder optimizeExpressions(boolean optimizeExpressions) {
            this.optimizeExpressions = optimizeExpressions;
            return this;
        }

        public SearchParameter build() {
            return new SearchParameter(this);
        }
    }
}
