/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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

    private List<SortOption> sortOptions = new ArrayList<>();
    private Integer maxResults;
    private Long maxTrackTotalHits;
    private Integer pageSize;
    private boolean permissionCheckEnabled = true;
    private boolean ignoreState;
    private boolean ignoreDeleted = true;
    private Integer pageIndex;
    private String language;
    private List<AggregationField> aggregationFields = new ArrayList<>();
    private int defaultAggregationMaxCount = 10;
    private List<String> fieldsToResolve = new ArrayList<>();
    private boolean optimizeExpressions;

    /**
     * Default constructor
     */
    public SearchParameter() {
    }

    /**
     * Constructor
     * @param sortOptions Result Sort options (in descending priority)
     */
    public SearchParameter(SortOption... sortOptions) {
        this.sortOptions = Arrays.asList(sortOptions);
    }

    /**
     * Constructor
     * @param aggregationFields Aggregation fields
     */
    public SearchParameter(AggregationField... aggregationFields) {
        this.aggregationFields = Arrays.asList(aggregationFields);
    }

    /**
     * Constructor
     * @param sortOptions Result Sort options (in descending priority)
     * @param aggregationFields Aggregation fields
     */
    public SearchParameter(List<SortOption> sortOptions, List<AggregationField> aggregationFields) {
        this.sortOptions = sortOptions;
        this.aggregationFields = aggregationFields;
    }

    /**
     * Constructor
     * @param language Search language
     */
    public SearchParameter(String language) {
        this.language = language;
    }

    /**
     * Constructor
     * @param language Search language
     * @param sortOptions Result Sort options (in descending priority)
     */
    public SearchParameter(String language, SortOption... sortOptions) {
        this(sortOptions);
        this.language = language;
    }

    /**
     * Gets the result sort options (in descending priority)
     * @return Result sort options (in descending priority)
     */
    public List<SortOption> getSortOptions() {
        return sortOptions;
    }

    /**
     * Sets the result sort options (in descending priority)
     * @param sortOptions Result sort options (in descending priority)
     */
    public void setSortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
    }

    /**
     * Adds a result sort option
     * @param sortOption Result sort option to add
     */
    public void addSortOption(SortOption sortOption) {
        sortOptions.add(sortOption);
    }

    /**
     * Gets the maximum number of results
     * @return Maximum number of results
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the maximum number of results
     * @param maxResults Maximum number of results
     */
    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Gets the maximum number of total hits to track. If this number is exceeded by the number of current search hits tracking will be stopped and the total
     * hits count will be marked as not exact.
     * @return Maximum number of total hits to track (null = use default value)
     */
    public Long getMaxTrackTotalHits() {
        return maxTrackTotalHits;
    }

    /**
     * Sets the maximum number of total hits to track. If this number is exceeded by the number of current search hits tracking will be stopped and the total
     * hits count will be marked as not exact.
     * @param maxTrackTotalHits Maximum number of total hits to track (null = use default value)
     */
    public void setMaxTrackTotalHits(Long maxTrackTotalHits) {
        this.maxTrackTotalHits = maxTrackTotalHits;
    }

    /**
     * Gets the pagination page size
     * @return Pagination page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the pagination page size
     * @param pageSize Pagination page size
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets the pagination page index
     * @return Pagination page index (starts with 1)
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

     /**
     * Sets the pagination page index
     * @param pageIndex Pagination page index (starts with 1)
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * Gets if permission checks are enabled (default=true)
     * @return TRUE if permission checks are enabled (default=true)
     */
    public boolean isPermissionCheckEnabled() {
        return permissionCheckEnabled;
    }

    /**
     * Sets if permission checks are enabled (default=true)
     * @param permissionCheckEnabled TRUE if permission checks are enabled (default=true)
     */
    public void setPermissionCheckEnabled(boolean permissionCheckEnabled) {
        this.permissionCheckEnabled = permissionCheckEnabled;
    }

    /**
     * Gets if the state of the record should be ignored (default=false)
     * @return TRUE if the state of the record should be ignored (default=false)
     */
    public boolean isIgnoreState() {
        return ignoreState;
    }

    /**
     * Sets if the state of the record should be ignored (default=false)
     * @param ignoreState TRUE if the state of the record should be ignored (default=false)
     */
    public void setIgnoreState(boolean ignoreState) {
        this.ignoreState = ignoreState;
    }

    /**
     * Gets if deleted records (state) should be ignored (default=false)
     * @return TRUE if deleted records (state) should be ignored (default=false)
     */
    public boolean isIgnoreDeleted() {
        return ignoreDeleted;
    }

    /**
     * Sets if deleted records (state) should be ignored (default=false)
     * @param ignoreDeleted TRUE if deleted records (state) should be ignored (default=false)
     */
    public void setIgnoreDeleted(boolean ignoreDeleted) {
        this.ignoreDeleted = ignoreDeleted;
    }

    /**
     * Gets the search language
     * @return Search language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the search language
     * @param language Search language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the aggregation fields
     * @return List of aggregation fields
     */
    public List<AggregationField> getAggregationFields() {
        return aggregationFields;
    }

    /**
     * Sets the aggregation fields
     * @param aggregationFields List of aggregation fields
     */
    public void setAggregationFields(List<AggregationField> aggregationFields) {
        this.aggregationFields = aggregationFields;
    }

    /**
     * Gets default maximum count of aggregation results for a field
     * @return Default maximum count of aggregation results for a field
     */
    public int getDefaultAggregationMaxCount() {
        return defaultAggregationMaxCount;
    }

    /**
     * Sets the default maximum count of aggregation results for a field
     * @param defaultAggregationMaxCount default maximum count of aggregation results for a field
     */
    public void setDefaultAggregationMaxCount(int defaultAggregationMaxCount) {
        this.defaultAggregationMaxCount = defaultAggregationMaxCount;
    }

    public void setFieldsToResolve(List<String> fieldsToResolve) {
        this.fieldsToResolve = fieldsToResolve;
    }

    public List<String> getFieldsToResolve() {
        return fieldsToResolve;
    }

    /**
     * Checks if expressions should be optimized.
     *
     * @see de.picturesafe.search.expression.Expression#optimize()
     *
     * @return true if expressions should be optimized
     */
    public boolean isOptimizeExpressions() {
        return optimizeExpressions;
    }

    /**
     * Sets if expressions should be optimized.
     *
     * @see de.picturesafe.search.expression.Expression#optimize()
     *
     * @param optimizeExpressions true if expressions should be optimized
     */
    public void setOptimizeExpressions(boolean optimizeExpressions) {
        this.optimizeExpressions = optimizeExpressions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("sortOptions", sortOptions)
                .append("maxResults", maxResults)
                .append("maxTrackTotalHits", maxTrackTotalHits)
                .append("pageSize", pageSize)
                .append("pageIndex", pageIndex)
                .append("permissionCheckEnabled", permissionCheckEnabled)
                .append("ignoreState", ignoreState)
                .append("ignoreDeleted", ignoreDeleted)
                .append("aggregationFields", aggregationFields)
                .append("defaultAggregationMaxCount", defaultAggregationMaxCount)
                .append("language", language)
                .append("fieldsToResolve", fieldsToResolve)
                .toString();
    }
}
