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
import de.picturesafe.search.elasticsearch.connect.dto.SearchHitDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.IsNullExpression;
import de.picturesafe.search.expression.OperationExpression;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ReferenceIT extends AbstractElasticIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceIT.class);
    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_SORT_ORDER = "sortOrder";
    private static final String FIELD_LINKING_TIME = "linkingTime";
    private static final String FIELD_NOTE = "note";

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    /**
     * Spezifische Daten im Index eintragen und indexieren
     */
    @Before
    public void begin() throws Exception {
        indexSetup.createIndex(indexAlias);

        final Map<String, Object> data = new HashMap<>();
        data.put("id", "20");
        final List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(5, 2L, 1L));
        references.add(generateReference(10, 2L, null));
        data.put("referenceWithSort", references);

        final Map<String, Object> data2 = new HashMap<>();
        data2.put("id", "21");
        final List<Map<String, Object>> references2 = new ArrayList<>();
        references2.add(generateReference(5, 1L, 2L));
        data2.put("referenceWithSort", references2);

        final Map<String, Object> data3 = new HashMap<>();
        data3.put("id", "22");
        final List<Map<String, Object>> references3 = new ArrayList<>();
        references3.add(generateReference(9, 2L, null));
        data3.put("referenceWithSort", references3);

        elasticsearch.addToIndex(data, mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(data2, mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(data3, mappingConfiguration, indexAlias, true);

        indexSetup.dumpIndexData(restClient, indexAlias);
    }

    private Map<String, Object> generateReference(final Integer target, final Long sortOrder, final Long linkingTime) {
        final Map<String, Object> doc = new HashMap<>();
        doc.put(FIELD_TARGET_ID, target);
        doc.put(FIELD_NOTE, "note " + sortOrder);
        if (sortOrder != null) {
            doc.put(FIELD_SORT_ORDER, sortOrder);
        }
        if (linkingTime != null) {
            doc.put(FIELD_LINKING_TIME, linkingTime);
        }

        return doc;
    }

    @Test
    public void testUnsortedIndex() {
        final Map<String, Object> data = new HashMap<>();
        data.put("id", "20");
        final List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(5, null, null));
        references.add(generateReference(10, null, null));
        data.put("referenceWithSort", references);

        elasticsearch.addToIndex(data, mappingConfiguration, indexAlias, true);
    }

    @Test
    public void testSearchValue() {
        final Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "23");
        List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(7, null, null));
        doc1.put("referenceWithSort", references);

        final Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "24");
        references = new ArrayList<>();
        references.add(generateReference(8, null, null));
        doc2.put("referenceWithSort", references);

        elasticsearch.addToIndex(Arrays.asList(doc1, doc2), mappingConfiguration, indexAlias, true, true);

        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 7);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, null, Locale.GERMAN);
        final SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("should find only one doc", 1, result.getTotalHitCount());
        assertEquals("did not find reference", "23", result.getHits().get(0).getId());
    }

    @Test
    public void testSearchIn() {
        final Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "123");
        List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(7, null, null));
        references.add(generateReference(8, null, null));
        doc1.put("referenceWithSort", references);

        final Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "124");
        references = new ArrayList<>();
        references.add(generateReference(8, null, null));
        doc2.put("referenceWithSort", references);

        elasticsearch.addToIndex(Arrays.asList(doc1, doc2), mappingConfiguration, indexAlias, true, true);

        Expression expression = new InExpression("referenceWithSort." + FIELD_TARGET_ID, 7);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, null, Locale.GERMAN);
        SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("should find only one doc", 1, result.getTotalHitCount());
        assertEquals("did not find reference", "123", result.getHits().get(0).getId());

        expression = new InExpression("referenceWithSort." + FIELD_TARGET_ID, 7, 8);
        queryDto = new QueryDto(expression, queryRangeDto, null, null, Locale.GERMAN);
        result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("should find two docs", 2, result.getTotalHitCount());
    }

    @Test
    public void testSearchEmptyIn() {
        final Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "223");
        doc1.put("caption", "SearchEmptyIn");
        List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(7, null, null));
        references.add(generateReference(8, null, null));
        doc1.put("referenceWithSort", references);

        final Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "224");
        doc2.put("caption", "SearchEmptyIn");
        references = new ArrayList<>();
        references.add(generateReference(8, null, null));
        doc2.put("referenceWithSort", references);

        elasticsearch.addToIndex(Arrays.asList(doc1, doc2), mappingConfiguration, indexAlias, true, true);

        final OperationExpression expression = OperationExpression.and(
                new ValueExpression("caption", "SearchEmptyIn"),
                new InExpression("referenceWithSort." + FIELD_TARGET_ID));
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, null, Locale.GERMAN);
        final SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("should find two docs", 2, result.getTotalHitCount());
    }

    @Test
    public void testSearchIsNull() {
        final Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "323");
        doc1.put("caption", "SearchIsNull");
        final List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(7, null, null));
        doc1.put("referenceWithSort", references);

        final Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "324");
        doc2.put("caption", "SearchIsNull");

        elasticsearch.addToIndex(Arrays.asList(doc1, doc2), mappingConfiguration, indexAlias, true, true);

        final OperationExpression expression = OperationExpression.and(
                new ValueExpression("caption", "SearchIsNull"),
                new IsNullExpression("referenceWithSort." + FIELD_TARGET_ID));
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, null, Locale.GERMAN);
        final SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("should find only one doc", 1, result.getTotalHitCount());
    }

    @Test
    public void testSortOnSortOrder() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = Collections.singletonList(SortOption.asc("referenceWithSort." + FIELD_SORT_ORDER));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionsAsc, null, Locale.GERMAN);
        final SearchResultDto sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResult.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "21", sortedResult.getHits().get(0).getId());
        assertEquals("result is not sorted", "20", sortedResult.getHits().get(1).getId());

        final List<SortOption> sortOptionsDesc = Collections.singletonList(SortOption.desc("referenceWithSort." + FIELD_SORT_ORDER));
        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, sortOptionsDesc, null, Locale.GERMAN);
        final SearchResultDto sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResultDesc.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "20", sortedResultDesc.getHits().get(0).getId());
        assertEquals("result is not sorted", "21", sortedResultDesc.getHits().get(1).getId());
    }

    @Test
    public void testSortOnLinkingTimeFilteredByTargetId() {
        final Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "1001");
        List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(777, null, 1L));
        references.add(generateReference(888, null, 3L));
        doc1.put("referenceWithSort", references);

        final Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "1002");
        references = new ArrayList<>();
        references.add(generateReference(888, null, 2L));
        doc2.put("referenceWithSort", references);

        elasticsearch.addToIndex(Arrays.asList(doc1, doc2), mappingConfiguration, indexAlias, true, true);

        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 888);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final SortOption sortOption =  SortOption.asc("referenceWithSort." + FIELD_LINKING_TIME);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, Collections.singletonList(sortOption), null, Locale.GERMAN);
        final SearchResultDto result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("result is not sorted correctly", "1001", result.getHits().get(0).getId());
        assertEquals("result is not sorted correctly", "1002", result.getHits().get(1).getId());

        final SortOption filteredSortOption =  SortOption.asc("referenceWithSort." + FIELD_LINKING_TIME)
                .filter(new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 888));
        final QueryDto filteredQueryDto = new QueryDto(expression, queryRangeDto, Collections.singletonList(filteredSortOption), null, Locale.GERMAN);
        final SearchResultDto filteredResult = elasticsearch.search(filteredQueryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("result is not sorted correctly", "1002", filteredResult.getHits().get(0).getId());
        assertEquals("result is not sorted correctly", "1001", filteredResult.getHits().get(1).getId());
    }

    @Test
    public void testSortOnLinkingTime() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = Collections.singletonList(SortOption.asc("referenceWithSort." + FIELD_LINKING_TIME));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionsAsc, null, Locale.GERMAN);
        final SearchResultDto sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResult.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "20", sortedResult.getHits().get(0).getId());
        assertEquals("result is not sorted", "21", sortedResult.getHits().get(1).getId());

        final List<SortOption> sortOptionsDesc = Collections.singletonList(SortOption.desc("referenceWithSort." + FIELD_LINKING_TIME));
        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, sortOptionsDesc, null, Locale.GERMAN);
        final SearchResultDto sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResultDesc.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "21", sortedResultDesc.getHits().get(0).getId());
        assertEquals("result is not sorted", "20", sortedResultDesc.getHits().get(1).getId());
    }
    @Test
    public void testSortOnNote() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = Collections.singletonList(SortOption.asc("referenceWithSort." + FIELD_NOTE));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionsAsc, null, Locale.GERMAN);
        final SearchResultDto sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResult.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "21", sortedResult.getHits().get(0).getId());
        assertEquals("result is not sorted", "20", sortedResult.getHits().get(1).getId());

        final List<SortOption> sortOptionsDesc = Collections.singletonList(SortOption.desc("referenceWithSort." + FIELD_NOTE));
        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, sortOptionsDesc, null, Locale.GERMAN);
        final SearchResultDto sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResultDesc.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "20", sortedResultDesc.getHits().get(0).getId());
        assertEquals("result is not sorted", "21", sortedResultDesc.getHits().get(1).getId());
    }

    @Test
    public void testWithAdditionalExpression() {

        final OperationExpression idExpression = OperationExpression.or(
                new ValueExpression("id", "20"),
                new ValueExpression("id", "21"));
        final OperationExpression expression = OperationExpression.and(
                idExpression,
                new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5));

        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = Collections.singletonList(SortOption.asc("referenceWithSort." + FIELD_SORT_ORDER));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptionsAsc, null, Locale.GERMAN);
        final SearchResultDto sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (SearchHitDto searchHit : sortedResult.getHits()) {
            LOG.debug("{}", searchHit);
        }

        assertEquals("result is not sorted", "21", sortedResult.getHits().get(0).getId());
        assertEquals("result is not sorted", "20", sortedResult.getHits().get(1).getId());
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }
}
