/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.Expression;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static de.picturesafe.search.expression.OperationExpression.Operator.AND;
import static de.picturesafe.search.expression.OperationExpression.Operator.OR;
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
        indexSetup.setupIndex(indexAlias);

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
    public void testUnsortedSearch() {
        final Map<String, Object> data = new HashMap<>();
        data.put("id", 23);
        final List<Map<String, Object>> references = new ArrayList<>();
        references.add(generateReference(8, null, null));
        references.add(generateReference(7, null, null));
        data.put("referenceWithSort", references);

        elasticsearch.addToIndex(data, mappingConfiguration, indexAlias, true);

        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 8);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, null, null, Locale.GERMAN);
        final ElasticsearchResult unsortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals("did not find reference without sortOrder", 23, getId(unsortedResult.getHits().get(0)));
    }

    @Test
    public void testSortOnSortOrder() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = new ArrayList<>();
        sortOptionsAsc.add(new SortOption("referenceWithSort." + FIELD_SORT_ORDER, SortOption.Direction.ASC));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, sortOptionsAsc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResult.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 21, getId(sortedResult.getHits().get(0)));
        assertEquals("result is not sorted", 20, getId(sortedResult.getHits().get(1)));

        final List<SortOption> sortOptionsDesc = new ArrayList<>();
        sortOptionsDesc.add(new SortOption("referenceWithSort." + FIELD_SORT_ORDER, SortOption.Direction.DESC));

        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, null, sortOptionsDesc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResultDesc.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 20, getId(sortedResultDesc.getHits().get(0)));
        assertEquals("result is not sorted", 21, getId(sortedResultDesc.getHits().get(1)));
    }

    @Test
    public void testSortOnLinkingTime() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = new ArrayList<>();
        sortOptionsAsc.add(new SortOption("referenceWithSort." + FIELD_LINKING_TIME, SortOption.Direction.ASC));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, sortOptionsAsc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResult.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 20, getId(sortedResult.getHits().get(0)));
        assertEquals("result is not sorted", 21, getId(sortedResult.getHits().get(1)));

        final List<SortOption> sortOptionsDesc = new ArrayList<>();
        sortOptionsDesc.add(new SortOption("referenceWithSort." + FIELD_LINKING_TIME, SortOption.Direction.DESC));

        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, null, sortOptionsDesc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResultDesc.getHits()) {
            LOG.debug("Hit: " + searchHit.toString());
        }

        assertEquals("result is not sorted", 21, getId(sortedResultDesc.getHits().get(0)));
        assertEquals("result is not sorted", 20, getId(sortedResultDesc.getHits().get(1)));
    }
    @Test
    public void testSortOnNote() {
        final Expression expression = new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = new ArrayList<>();
        sortOptionsAsc.add(new SortOption("referenceWithSort." + FIELD_NOTE, SortOption.Direction.ASC));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, sortOptionsAsc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResult.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 21, getId(sortedResult.getHits().get(0)));
        assertEquals("result is not sorted", 20, getId(sortedResult.getHits().get(1)));

        final List<SortOption> sortOptionsDesc = new ArrayList<>();
        sortOptionsDesc.add(new SortOption("referenceWithSort." + FIELD_NOTE, SortOption.Direction.DESC));

        final QueryDto queryDtoDesc = new QueryDto(expression, queryRangeDto, null, sortOptionsDesc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResultDesc = elasticsearch.search(queryDtoDesc, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResultDesc.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 20, getId(sortedResultDesc.getHits().get(0)));
        assertEquals("result is not sorted", 21, getId(sortedResultDesc.getHits().get(1)));
    }

    @Test
    public void testWithAdditionalExpression() {

        final OperationExpression idExpression = OperationExpression.builder(OR)
                .add(new ValueExpression("id", "20"))
                .add(new ValueExpression("id", "21")).build();

        final OperationExpression expression = OperationExpression.builder(AND)
                .add(idExpression)
                .add(new ValueExpression("referenceWithSort." + FIELD_TARGET_ID, 5))
                .build();

        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 40);
        final List<SortOption> sortOptionsAsc = new ArrayList<>();
        sortOptionsAsc.add(new SortOption("referenceWithSort." + FIELD_SORT_ORDER, SortOption.Direction.ASC));
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, null, sortOptionsAsc, null, Locale.GERMAN);
        final ElasticsearchResult sortedResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        for (Map<String, Object> searchHit : sortedResult.getHits()) {
            LOG.debug(searchHit.toString());
        }

        assertEquals("result is not sorted", 21, getId(sortedResult.getHits().get(0)));
        assertEquals("result is not sorted", 20, getId(sortedResult.getHits().get(1)));
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }
}
