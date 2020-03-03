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

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Elasticsearch result
 */
public class SearchResult {

    private final List<SearchResultItem> searchResultItems;
    private final int pageIndex;
    private final int pageSize;
    private final int resultCount;
    private final long totalHitCount;
    private final boolean exactHitCount;

    private List<ResultFacet> facets = Collections.emptyList();

    /**
     * Constructor
     *
     * @param searchResultItems     Search result items
     * @param pageIndex             Page index (starts with 1)
     * @param pageSize              Page size
     * @param resultCount           Count of result items
     * @param totalHitCount         Total hit count
     * @param exactHitCount         Is total hit count exact number or does it mean "greater or equal"?
     */
    public SearchResult(List<SearchResultItem> searchResultItems, int pageIndex, int pageSize, int resultCount, long totalHitCount, boolean exactHitCount) {
        this.searchResultItems = searchResultItems;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.resultCount = resultCount;
        this.totalHitCount = totalHitCount;
        this.exactHitCount = exactHitCount;
    }

    /**
     * Constructor
     *
     * @param searchResultItems     Search result items
     * @param pageIndex             Page index (starts with 1)
     * @param pageSize              Page size
     * @param resultCount           Count of result items
     * @param totalHitCount         Total hit count
     * @param exactHitCount         Is total hit count exact number or does it mean "greater or equal"?
     * @param facets                Result facets
     */
    public SearchResult(List<SearchResultItem> searchResultItems, int pageIndex, int pageSize, int resultCount, long totalHitCount, boolean exactHitCount,
                        List<ResultFacet> facets) {
        this(searchResultItems, pageIndex, pageSize, resultCount, totalHitCount, exactHitCount);
        this.facets = facets;
    }

    /**
     * Gets the search result items.
     *
     * @return Search result items
     */
    public List<SearchResultItem> getSearchResultItems() {
        return searchResultItems;
    }

    /**
     * Gets the page index.
     *
     * @return Page index (starts with 1)
     */
    public int getPageIndex() {
        return pageIndex;
    }

    /**
     * Gets the page size.
     *
     * @return Page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Gets the page count.
     *
     * @return  Page count (number of result pages) depending on the result count and the page size
     */
    public int getPageCount() {
        int pageCount = 1;
        if (resultCount > 0 && pageSize > 0) {
            pageCount = resultCount / pageSize;
            if (resultCount % pageSize > 0) {
                pageCount += 1;
            }
        }
        return Math.max(1, pageCount);
    }

    /**
     * Gets the count of result items.
     *
     * @return Count of result items
     */
    public int getResultCount() {
        return resultCount;
    }

    /**
     * Gets the total hit count.
     *
     * @return Total hit count
     */
    public long getTotalHitCount() {
        return totalHitCount;
    }

    /**
     * Checks if total hit count exact number.
     *
     * @return TRUE if total hit count exact number, FALSE if total hit count is greater or equal the given number
     */
    public boolean isExactHitCount() {
        return exactHitCount;
    }

    /**
     * Gets the result facets.
     *
     * @return Result facets
     */
    public List<ResultFacet> getFacets() {
        return facets;
    }

    /**
     * Sets the result facets.
     *
     * @param facets Result facets
     */
    public void setFacets(List<ResultFacet> facets) {
        this.facets = facets;
    }

    /**
     * Gets the IDs of the result items.
     *
     * @return IDs of the result items
     */
    public List<Long> getIds() {
        final List<Long> ids = new ArrayList<>(searchResultItems.size());
        for (final SearchResultItem item : searchResultItems) {
            ids.add(item.getId());
        }
        return ids;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("searchResultItems", searchResultItems) //--
                .append("pageIndex", pageIndex) //--
                .append("pageSize", pageSize) //--
                .append("resultCount", resultCount) //--
                .append("totalHitCount", totalHitCount) //--
                .append("facets", facets) //--
                .toString();
    }
}
