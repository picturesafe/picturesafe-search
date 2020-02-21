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

package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SearchResultItemTest {

    @Test
    public void testGetAttribute() {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("number", 1);
        doc.put("string", "test");
        final SearchResultItem searchResultItem = new SearchResultItem(doc);
        assertEquals(1, searchResultItem.getAttribute("number"));
        assertEquals("test", searchResultItem.getAttribute("string"));
        assertNull(searchResultItem.getAttribute("does_not_exist"));
    }

    @Test
    public void testGetDateAttribute() {
        final Date date = new Date();
        final String dateString = ElasticDateUtils.formatIso(date, "Europe/Berlin");
        final Map<String, Object> doc = new HashMap<>();
        doc.put("date", date);
        doc.put("dateString", dateString);
        final SearchResultItem searchResultItem = new SearchResultItem(doc);
        assertEquals(date, searchResultItem.getDateAttribute("date"));
        assertEquals(date, searchResultItem.getDateAttribute("dateString"));
        assertNull(searchResultItem.getDateAttribute("does_not_exist"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateAttributeException() {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("date", 1);
        final SearchResultItem searchResultItem = new SearchResultItem(doc);
        searchResultItem.getDateAttribute("date");
    }

    @Test
    public void testGetLanguageAttribute() {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("title.de", "Deutscher Titel");
        doc.put("title.en", "English title");
        final SearchResultItem searchResultItem = new SearchResultItem(doc);
        assertEquals("Deutscher Titel", searchResultItem.getLanguageAttribute("title", Locale.GERMANY));
        assertEquals("English title", searchResultItem.getLanguageAttribute("title", Locale.UK));
        assertNull(searchResultItem.getLanguageAttribute("does_not_exist", Locale.ROOT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLanguageAttributeException() {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("title.de", 1);
        final SearchResultItem searchResultItem = new SearchResultItem(doc);
        searchResultItem.getLanguageAttribute("title", Locale.GERMANY);
    }
}
