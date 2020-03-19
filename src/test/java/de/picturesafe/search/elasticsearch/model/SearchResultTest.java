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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SearchResultTest {

    @Test
    public void testSearchResult() {
        final SearchResult searchResult =  getTestSearchResult(10, 100);
        assertEquals(1, searchResult.getPageIndex());
        assertEquals(10, searchResult.getPageSize());
        assertEquals(100, searchResult.getResultCount());
        assertEquals(1000, searchResult.getTotalHitCount());
        assertTrue(searchResult.isExactHitCount());
    }

    @Test
    public void testSearchResultItemIds() {
        SearchResult searchResult =  getTestSearchResult(10, 100);
        assertNotNull(searchResult.getIds());
        assertEquals(100, searchResult.getIds().size());
        searchResult =  getTestSearchResult(10, 0);
        assertNotNull(searchResult.getIds());
        assertEquals(0, searchResult.getIds().size());
    }

    @Test
    public void testPageSize() {
        SearchResult searchResult =  getTestSearchResult(10, 100);
        assertEquals(10, searchResult.getPageCount());
        searchResult =  getTestSearchResult(100, 100);
        assertEquals(1, searchResult.getPageCount());
        searchResult =  getTestSearchResult(1000, 100);
        assertEquals(1, searchResult.getPageCount());
        searchResult =  getTestSearchResult(10, 0);
        assertEquals(1, searchResult.getPageCount());
        searchResult =  getTestSearchResult(0, 100);
        assertEquals(1, searchResult.getPageCount());
        searchResult =  getTestSearchResult(10, 105);
        assertEquals(11, searchResult.getPageCount());
        searchResult =  getTestSearchResult(10, 110);
        assertEquals(11, searchResult.getPageCount());
        searchResult =  getTestSearchResult(10, 111);
        assertEquals(12, searchResult.getPageCount());
        searchResult =  getTestSearchResult(10, 5);
        assertEquals(1, searchResult.getPageCount());
    }

    private SearchResult getTestSearchResult(int pageSize, int resultCount) {
        return new SearchResult(getTestSearchResultItems(resultCount), 1, pageSize, resultCount, 1000, true);
    }

    private List<SearchResultItem> getTestSearchResultItems(int resultCount) {
        final List<SearchResultItem> testSearchResultItems = new ArrayList<>();
        for (long id = 1; id <= resultCount; id++) {
            testSearchResultItems.add(new SearchResultItem(DocumentBuilder.id(id).build()));
        }
        return testSearchResultItems;
    }
}
