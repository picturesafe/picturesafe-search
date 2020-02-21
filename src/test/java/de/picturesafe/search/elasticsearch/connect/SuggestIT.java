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

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.SuggestExpression;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SuggestIT extends AbstractElasticIntegrationTest {

    private static final String[] CAPTIONS = {
        "Dies ist ein schöner Titel",
        "Dies ist meiner Meinung nach der schönste Titel",
        "Dies ist meiner Meinung nach der schönste Titel",
        "Das ist ein schöner Titel",
        "Das ist kein Titel, es ist eine Caption",
        "Das Ende einer Caption",
        "Da steht die Caption",
        "Da steht die Caption",
        "Dort stehen schöne Titel",
        "Dieser Titel macht keinen Sinn",
        "Titel machen generell keinen Sinn",
        "Jetzt einen Cappuccino"
    };

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Before
    public void begin() {
        indexSetup.createIndex(indexAlias);

        final List<Map<String, Object>> docs = new ArrayList<>(CAPTIONS.length);
        int i = 1;
        for (String caption : CAPTIONS) {
            final Map<String, Object> document = new HashMap<>();
            document.put("id", i++);
            document.put("caption", caption);
            document.put("keyword", StringUtils.substringAfterLast(caption, " "));
            docs.add(document);
        }
        elasticsearch.addToIndex(docs, mappingConfiguration, indexAlias, true, true);
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testSuggestCaption() {
        SuggestExpression expression = new SuggestExpression("Dies", 10);
        Map<String, List<String>> result = elasticsearch.suggest(indexAlias, expression);
        assertTrue(result.containsKey("suggest"));
        List<String> suggestions = result.get("suggest");
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.contains("Dies ist ein schöner Titel"));
        assertTrue(suggestions.contains("Dies ist meiner Meinung nach der schönste Titel"));
        assertTrue(suggestions.contains("Dieser Titel macht keinen Sinn"));

        expression = new SuggestExpression("Da", 10);
        result = elasticsearch.suggest(indexAlias, expression);
        suggestions = result.get("suggest");
        assertEquals(4, suggestions.size());
        assertEquals("Da steht die Caption", suggestions.get(0));
        assertTrue(suggestions.contains("Das ist ein schöner Titel"));
        assertTrue(suggestions.contains("Das ist kein Titel, es ist eine Caption"));
        assertTrue(suggestions.contains("Das Ende einer Caption"));
    }

    @Test
    public void testSuggestKeyword() {
        SuggestExpression expression = new SuggestExpression("Tit", 10);
        Map<String, List<String>> result = elasticsearch.suggest(indexAlias, expression);
        assertTrue(result.containsKey("suggest"));
        List<String> suggestions = result.get("suggest");
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.contains("Titel"));
        assertTrue(suggestions.contains("Titel machen generell keinen Sinn"));

        expression = new SuggestExpression("Cap", 10);
        result = elasticsearch.suggest(indexAlias, expression);
        suggestions = result.get("suggest");
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.contains("Caption"));
        assertTrue(suggestions.contains("Cappuccino"));
    }

    @Test
    public void testSuggestCountLimit() {
        final SuggestExpression expression = new SuggestExpression("Da", 2);
        final Map<String, List<String>> result = elasticsearch.suggest(indexAlias, expression);
        assertTrue(result.containsKey("suggest"));
        final List<String> suggestions = result.get("suggest");
        assertEquals(2, suggestions.size());
    }
}
