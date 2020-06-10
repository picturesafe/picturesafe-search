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
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.expression.ConditionExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SortOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_ENDS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_STARTS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_WILDCARD;
import static org.junit.Assert.assertEquals;

public class WildcardTermIT extends AbstractElasticIntegrationTest {

    private static final String[] CAPTIONS = {
        "Dies ist ein schöner Titel",
        "Dies ist meiner Meinung nach der schönste Titel",
        "Dies ein schöner Titel ist",
        "Das ist ein schöner Titel",
        "Dort stehen schöne Titel",
        "Selbst das ist schon ein Titel"
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
            final Map<String, Object> document = DocumentBuilder.id(i++).put("caption", caption).build();
            docs.add(document);
        }
        elasticsearch.addToIndex(docs, mappingConfiguration, indexAlias, true, true);
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testStartsWith() {
        assertEquals(2, search(TERM_STARTS_WITH, "Dies ist").getTotalHitCount());
        assertEquals(3, search(TERM_STARTS_WITH, "Dies").getTotalHitCount());
        assertEquals(1, search(TERM_STARTS_WITH, "Das ist").getTotalHitCount());
        assertEquals(0, search(TERM_STARTS_WITH, "schöner Titel").getTotalHitCount());

        // Wildcard searches currently take place on keyword fields and are therefore case-sensitive.
        assertEquals(0, search(TERM_STARTS_WITH, "dies").getTotalHitCount());
    }

    @Test
    public void testNotStartsWith() {
        assertEquals(4, search(TERM_STARTS_WITH, "Dies ist", true).getTotalHitCount());
        assertEquals(3, search(TERM_STARTS_WITH, "Dies", true).getTotalHitCount());
        assertEquals(6, search(TERM_STARTS_WITH, "schöner Titel", true).getTotalHitCount());
    }

    @Test
    public void testEndsWith() {
        assertEquals(5, search(TERM_ENDS_WITH, "Titel").getTotalHitCount());
        assertEquals(2, search(TERM_ENDS_WITH, "schöner Titel").getTotalHitCount());
        assertEquals(1, search(TERM_ENDS_WITH, "Titel ist").getTotalHitCount());
        assertEquals(0, search(TERM_ENDS_WITH, "Dies").getTotalHitCount());

        // Wildcard searches currently take place on keyword fields and are therefore case-sensitive.
        assertEquals(0, search(TERM_ENDS_WITH, "titel").getTotalHitCount());
    }

    @Test
    public void testNotEndsWith() {
        assertEquals(1, search(TERM_ENDS_WITH, "Titel", true).getTotalHitCount());
        assertEquals(4, search(TERM_ENDS_WITH, "schöner Titel", true).getTotalHitCount());
        assertEquals(6, search(TERM_ENDS_WITH, "Dies", true).getTotalHitCount());
    }

    @Test
    public void testWildcard() {
        assertEquals(2, search(TERM_WILDCARD, "Dies*Titel").getTotalHitCount());
        assertEquals(2, search(TERM_WILDCARD, "Dies*schön*Titel").getTotalHitCount());
        assertEquals(3, search(TERM_WILDCARD, "Dies*schön*Titel*").getTotalHitCount());
        assertEquals(4, search(TERM_WILDCARD, "*schön*Titel").getTotalHitCount());
        assertEquals(5, search(TERM_WILDCARD, "*sch?n* Titel").getTotalHitCount());
        assertEquals(6, search(TERM_WILDCARD, "*sch?n* Titel*").getTotalHitCount());

        // Wildcard searches currently take place on keyword fields and are therefore case-sensitive.
        assertEquals(0, search(TERM_WILDCARD, "*titel*").getTotalHitCount());
    }

    private SearchResultDto search(ConditionExpression.Comparison comparison, String term) {
        return search(comparison, term, false);
    }

    private SearchResultDto search(ConditionExpression.Comparison comparison, String term, boolean negate) {
        Expression expression = new ValueExpression("caption", comparison, term);
        if (negate) {
            expression = new MustNotExpression(expression);
        }

        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
        final List<SortOption> sortOptionList = new ArrayList<>();
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionList, null, Locale.GERMAN);
        return elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
    }
}
