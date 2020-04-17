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
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.expression.Expression;
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

import static de.picturesafe.search.expression.ConditionExpression.Comparison.LIKE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_LIKE;
import static org.junit.Assert.assertEquals;

public class LikeIT extends AbstractElasticIntegrationTest {

    private static final String[] CAPTIONS = {
        "Das Coronavirus legt alles lahm.",
        "Wegen dem Coronavirus bleiben wir zu Hause.",
        "Mit einer Virusinfektion ist nicht zu spaßen!",
        "Oder heißt es Vireninfektion?",
        "Ein Virus kann lästig sein!",
        "Ich trinke kein Corona Bier."
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
    public void testLike() {
        assertEquals(3, search("corona*", false).getTotalHitCount());
        assertEquals(2, search("virus*", false).getTotalHitCount());
        assertEquals(4, search("*virus*", false).getTotalHitCount());
        assertEquals(2, search("vir??infektion", false).getTotalHitCount());
    }

    @Test
    public void testNotLike() {
        assertEquals(3, search("corona*", true).getTotalHitCount());
        assertEquals(4, search("virus*", true).getTotalHitCount());
        assertEquals(2, search("*virus*", true).getTotalHitCount());
        assertEquals(4, search("vir??infektion", true).getTotalHitCount());
    }

    private ElasticsearchResult search(String query, boolean negate) {
        final ValueExpression.Comparison comparison = negate ? NOT_LIKE : LIKE;
        final Expression expression = new ValueExpression("caption", comparison, query);

        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
        final List<SortOption> sortOptionList = new ArrayList<>();
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionList, null, Locale.GERMAN);
        return elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
    }
}
