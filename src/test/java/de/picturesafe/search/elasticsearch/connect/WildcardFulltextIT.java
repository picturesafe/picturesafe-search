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

import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SortOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class WildcardFulltextIT extends AbstractElasticIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIT.class);

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Before
    public void setup() {
        indexSetup.createIndex(indexAlias);

        final List<Map<String, Object>> docs = Arrays.asList(
            DocumentBuilder.id(1).put("location", "Hamburg Altona").build(),
            DocumentBuilder.id(2).put("location", "Bremen").build(),
            DocumentBuilder.id(3).put("location", "Rostock").build(),
            DocumentBuilder.id(4).put("location", "Bosnien Herzegowina").build());
        elasticsearch.addToIndex(indexAlias, true, true, docs);
    }

    @After
    public void tearDown() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testHyphen() {
        for (TestData testData : createTestData()) {
            final Expression expression = new ValueExpression(testData.getField(), testData.getQuery());

            final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
            final List<SortOption> sortOptionList = new ArrayList<>();
            final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionList, null, Locale.GERMAN);

            final SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

            LOG.debug("result.getCount()=" + result.getTotalHitCount());

            assertEquals("There are not as many results as expected (query=" + testData.getQuery() + "): ",
                    testData.getExpectedResults(), result.getTotalHitCount());
        }
    }

    private TestData[] createTestData() {
        return new TestData[] {
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "Altona", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*Altona", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*ltona", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "Altona*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "Alton*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*Altona*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*lton*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*Alt*ona*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*lt*on*", 1),
                new TestData(FieldConfiguration.FIELD_NAME_FULLTEXT, "*Al*na*", 1),

                new TestData("location", "Altona", 1),
                new TestData("location", "*Altona", 1),
                new TestData("location", "*ltona", 1),
                new TestData("location", "Altona*", 1),
                new TestData("location", "Alton*", 1),
                new TestData("location", "*Altona*", 1),
                new TestData("location", "*lton*", 1),
                new TestData("location", "*Alt*ona*", 1),
                new TestData("location", "*lt*on*", 1),
                new TestData("location", "*Al*na*", 1),
        };
    }

    private static class TestData {
        final String field;
        final String query;
        final int expectedResults;

        TestData(String field, String query, int expectedResults) {
            this.field = field;
            this.query = query;
            this.expectedResults = expectedResults;
        }

        private String getField() {
            return field;
        }

        public String getQuery() {
            return query;
        }

        public int getExpectedResults() {
            return expectedResults;
        }
    }
}
