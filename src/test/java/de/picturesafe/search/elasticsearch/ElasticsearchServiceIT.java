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

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.config.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.error.QuerySyntaxException;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.AccountContext;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.AggregationField;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.Version;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, ElasticsearchServiceIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class ElasticsearchServiceIT {

    @Autowired
    private IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    RestClientConfiguration elasticsearchRestClientConfiguration;

    private RestHighLevelClient restClient;
    private String indexName;
    private String indexAlias;

    @Before
    public final void setup() {
        this.indexAlias = indexPresetConfiguration.getIndexAlias();
        this.restClient = elasticsearchRestClientConfiguration.getClient();
        indexName = null;
    }

    @After
    public void cleanup() {
        if (elasticsearchService.aliasExists(indexAlias)) {
            elasticsearchService.removeAlias(indexAlias);
        }
        if (indexName != null) {
            elasticsearchService.deleteIndex(indexName);
        }
    }

    @Test
    public void testCreateIndex() {
        indexName = elasticsearchService.createIndex(indexAlias);
        assertTrue(indexName.startsWith(indexAlias));
    }

    @Test
    public void testAddFieldConfiguration() throws IOException {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);

        final String fieldName = "add_field_test";
        elasticsearchService.addFieldConfiguration(indexAlias, StandardFieldConfiguration.builder(fieldName, ElasticsearchType.TEXT).copyToFulltext(true).build());

        final GetMappingsResponse response = restClient.indices().getMapping(new GetMappingsRequest().indices(indexName), RequestOptions.DEFAULT);
        final MappingMetaData mapping = response.mappings().get(indexName);
        final Map<String, Object> properties = (Map<String, Object>) mapping.sourceAsMap().get("properties");
        assertTrue("Mapping should contain new field", properties.containsKey(fieldName));
    }

    @Test
    public void testCreateAlias() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        assertTrue(elasticsearchService.aliasExists(indexAlias));
    }

    @Test
    public void testRemoveAlias() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        assertEquals(indexName, elasticsearchService.removeAlias(indexAlias));
    }

    @Test
    public void testResolveIndexNames() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);

        final List<String> indexNames = elasticsearchService.resolveIndexNames(indexAlias);
        assertEquals(1, indexNames.size());
        assertEquals(indexName, indexNames.get(0));
    }

    @Test
    public void testIndexVersion() {
        final int indexVersion = 4711;
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        elasticsearchService.setIndexVersion(indexAlias, indexVersion);
        assertEquals(indexVersion, elasticsearchService.getIndexVersion(indexAlias));
    }

    @Test
    public void testAddToIndex() throws Exception {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc = createDocument(System.currentTimeMillis());
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc);

        final GetResponse response = restClient.get(new GetRequest(indexAlias).id(String.valueOf(getId(doc))),
                RequestOptions.DEFAULT);
        assertTrue("Document added cannot be found!", response.isExists());
        assertDocsAreEqual(doc, response.getSource());
    }

    @Test
    public void testAddToIndexMultiple() throws Exception {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(1);
        final Map<String, Object> doc2 = createDocument(2);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        GetResponse response = restClient.get(new GetRequest().index(indexAlias).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        assertDocsAreEqual(doc1, response.getSource());
        response = restClient.get(new GetRequest().index(indexAlias).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());
        assertDocsAreEqual(doc2, response.getSource());
    }

    @Test
    public void testRemoveFromIndex() throws Exception {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(1);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc1);
        final Map<String, Object> doc2 = createDocument(2);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc2);

        GetResponse response = restClient.get(new GetRequest().index(indexAlias).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());

        elasticsearchService.removeFromIndex(indexAlias, DataChangeProcessingMode.BLOCKING, 1);
        response = restClient.get(new GetRequest().index(indexAlias).id("1"), RequestOptions.DEFAULT);
        assertFalse("Document #1 should be deleted!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 should still exist!", response.isExists());
    }

    @Test
    public void testRemoveFromIndexMultiple() throws Exception {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(1);
        final Map<String, Object> doc2 = createDocument(2);
        final Map<String, Object> doc3 = createDocument(3);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3));

        GetResponse response = restClient.get(new GetRequest().index(indexAlias).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("3"), RequestOptions.DEFAULT);
        assertTrue("Document #3 cannot be found!", response.isExists());

        elasticsearchService.removeFromIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(1L, 3L));
        response = restClient.get(new GetRequest().index(indexAlias).id("1"), RequestOptions.DEFAULT);
        assertFalse("Document #1 should be deleted!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 should still exist!", response.isExists());
        response = restClient.get(new GetRequest().index(indexAlias).id("3"), RequestOptions.DEFAULT);
        assertFalse("Document #3 should be deleted!", response.isExists());
    }

    @Test
    public void testSearchSimple() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("title", "Hund"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        SearchResultItem item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("title", "Katze"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new FulltextExpression("Vögel"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new FulltextExpression("Hamburg"),
                new SearchParameter(new SortOption("id", SortOption.Direction.DESC)));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new FulltextExpression("Hamburg"),
                new SearchParameter(new SortOption("id", SortOption.Direction.ASC)));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());
    }

    @Test
    public void testQuerySyntaxException() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc);

        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(),
                new ValueExpression("title", "Hund"), new SearchParameter());
        assertEquals(1, result.getTotalHitCount());

        try {
            elasticsearchService.search(indexAlias, new AccountContext(),
                    new ValueExpression("title", "/"), new SearchParameter());
            fail("Expected an QuerySyntaxException to be thrown!");
        } catch (QuerySyntaxException qse) {
            assertEquals("/", qse.getInvalidQueryString());
        }

        try {
            elasticsearchService.search(indexAlias, new AccountContext(),
                    new ValueExpression("title", "(Hund OR Schwanz) AND (Hamburg"), new SearchParameter());
            fail("Expected an QuerySyntaxException to be thrown!");
        } catch (QuerySyntaxException qse) {
            assertEquals("(Hund OR Schwanz) AND (Hamburg", qse.getInvalidQueryString());
        }
    }

    @Test
    public void testSearchMultilang() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        doc1.put("text_multilang.de", "Dies ist ein deutscher Text.");
        doc1.put("text_multilang.en", "This is an english text.");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg");
        doc2.put("text_multilang.de", "Ich spreche Deutsch.");
        doc2.put("text_multilang.en", "I speak english.");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("text_multilang", "deutsch"),
                new SearchParameter("de"));
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("text_multilang", "deutsch"),
                new SearchParameter("en"));
        assertEquals(0, result.getTotalHitCount());
        assertEquals(0, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("text_multilang", "english"),
                new SearchParameter("en"));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new FulltextExpression("deutscher"),
                new SearchParameter("de"));
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(), new FulltextExpression("english"),
                new SearchParameter("en"));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
    }

    @Test
    public void testSortMultilang() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Äöü Multilang-Test 1");
        doc1.put("text_multilang.de", "Äöü Test");
        final Map<String, Object> doc2 = createDocument(4712, "Aou Multilang-Test 2");
        doc2.put("text_multilang.de", "Aou Test");
        final Map<String, Object> doc3 = createDocument(4713, "Äöü Multilang-Best 3");
        doc3.put("text_multilang.de", "Äöü Best");
        final Map<String, Object> doc4 = createDocument(4714, "Bei Multilang-Test 4");
        doc4.put("text_multilang.de", "Bei Test");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        final SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("title", "Multilang"),
                new SearchParameter("de", new SortOption("text_multilang")));
        assertEquals(4, result.getResultCount());
        assertEquals(4713, result.getSearchResultItems().get(0).getId());
        assertEquals(4712, result.getSearchResultItems().get(1).getId());
        assertEquals(4711, result.getSearchResultItems().get(2).getId());
        assertEquals(4714, result.getSearchResultItems().get(3).getId());
    }

    @Test
    public void testSearchFacets() throws Exception {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg",
                DateUtils.parseDate("10.05.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg",
                DateUtils.parseDate("01.05.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc3 = createDocument(4713, "Die Maus versteckt sich vor der Katze in Hamburg",
                DateUtils.parseDate("01.01.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc4 = createDocument(4714, "Die Entwickler feiert Geburtstag in Buchholz",
                DateUtils.parseDate("28.07.2018", "dd.MM.yyyy"), "Buchholz");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("name", "name"),
                new SearchParameter(new AggregationField("createDate", 1000), new AggregationField("location", 10)));
        assertEquals(4, result.getTotalHitCount());
        assertEquals(4, result.getResultCount());
        assertEquals(3, result.getFacets().size());

        final ResultFacet locationFacet = getFacet(result, "location");
        assertNotNull(locationFacet);
        assertEquals(4, locationFacet.getCount());
        ResultFacetItem facetItem = locationFacet.getFacetItems().get(0);
        assertEquals("Hamburg", facetItem.getValue());
        assertEquals(3, facetItem.getCount());
        facetItem = locationFacet.getFacetItems().get(1);
        assertEquals("Buchholz", facetItem.getValue());
        assertEquals(1, facetItem.getCount());

        final ResultFacet yearsFacet = getFacet(result, "years");
        assertNotNull(yearsFacet);
        assertEquals(4, yearsFacet.getCount());
        final Map<String, ResultFacetItem> years = yearsFacet.getFacetItems().stream().collect(Collectors.toMap(f -> f.getValue().toString(), f -> f));
        final ResultFacetItem year2019 = years.get("2019");
        assertEquals(3, year2019.getCount());
        final ResultFacetItem year2018 = years.get("2018");
        assertEquals(1, year2018.getCount());
    }

    @Test
    public void testInExpressionSearches() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711);
        final Map<String, Object> doc2 = createDocument(4712);
        final Map<String, Object> doc3 = createDocument(4713);
        final Map<String, Object> doc4 = createDocument(4714);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        final int[] intArray = new int[] {4711, 4714};
        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("id", intArray),
                new SearchParameter());
        assertEquals(2, result.getResultCount());

        final String[] stringArray = new String[] {"name-4711", "name-4713", "name-4714"};
        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("name", stringArray),
                new SearchParameter());
        assertEquals(3, result.getResultCount());

        final Long[] longArray = new Long[] {4711L, 4712L, 4714L};
        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("id", longArray),
                new SearchParameter());
        assertEquals(3, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("id", new int[] {4711}),
                new SearchParameter());
        assertEquals(1, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("name", "name-4711"),
                new SearchParameter());
        assertEquals(1, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new InExpression("name", "name-4711","name-4714"),
                new SearchParameter());
        assertEquals(2, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId());
        assertEquals(4714, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testNotInExpressionSearches() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711);
        final Map<String, Object> doc2 = createDocument(4712);
        final Map<String, Object> doc3 = createDocument(4713);
        final Map<String, Object> doc4 = createDocument(4714);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        final int[] intArray = new int[] {4711, 4712, 4714};
        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("id", intArray)),
                new SearchParameter());
        assertEquals(1, result.getResultCount());
        assertEquals(4713, result.getSearchResultItems().get(0).getId());

        final String[] stringArray = new String[] {"name-4711", "name-4713", "name-4714"};
        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("name", stringArray)),
                new SearchParameter());
        assertEquals(1, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId());

        final Long[] longArray = new Long[] {4711L, 4712L, 4714L};
        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("id", longArray)),
                new SearchParameter());
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("id", new int[] {4711})),
                new SearchParameter());
        assertEquals(3, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId());
        assertEquals(4713, result.getSearchResultItems().get(1).getId());
        assertEquals(4714, result.getSearchResultItems().get(2).getId());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("name", "name-4711")),
                new SearchParameter());
        assertEquals(3, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new AccountContext(),
                new MustNotExpression(new InExpression("name", "name-4711","name-4714")),
                new SearchParameter());
        assertEquals(2, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId());
        assertEquals(4713, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testGetDocument() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc = createDocument(System.currentTimeMillis());
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc);

        final Map<String, Object> indexDoc = elasticsearchService.getDocument(indexAlias, getId(doc));
        assertDocsAreEqual(doc, indexDoc);
    }

    @Test
    public void testMaxTrackTotalHits() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Der Hund beißt die Katze Hamburg");
        final Map<String, Object> doc3 = createDocument(4713, "Der Hund kackt auf den Gehweg Hamburg");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3));

        final SearchParameter searchParameter = new SearchParameter();
        searchParameter.setMaxResults(1);
        searchParameter.setMaxTrackTotalHits(2L);
        SearchResult result = elasticsearchService.search(indexAlias, new AccountContext(), new ValueExpression("title", "Hund"),
                searchParameter);
        assertEquals(1, result.getResultCount());
        assertEquals(2, result.getTotalHitCount());
        assertFalse(result.isExactHitCount());
    }

    @Test
    public void testElasticsearchInfo() {
        final ElasticsearchInfo elasticsearchInfo = elasticsearchService.getElasticsearchInfo();
        assertNotNull(elasticsearchInfo.getClientVersion());
        assertNotNull(elasticsearchInfo.getServerVersion());
        assertNotNull(elasticsearchInfo.getClusterName());
        assertEquals(Version.CURRENT.toString(), elasticsearchInfo.getClientVersion());
        assertTrue(elasticsearchInfo.getServerVersion().length() > 0);
        assertTrue(elasticsearchInfo.getClusterName().length() > 0);
    }

    private Map<String, Object> createDocument(long id) {
        return createDocument(id, "Document title #" + id);
    }

    private Map<String, Object> createDocument(long id, String title) {
        return createDocument(id, title, new Date(), "Hamburg");
    }

    private Map<String, Object> createDocument(long id, String title, Date createDate, String location) {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("id",id);
        doc.put("name", "name-" + id);
        doc.put("title", title);
        doc.put("caption", "Document caption #" + id + "\nThis document was created for testing purposes.\nÄÖÜäöüß");
        doc.put("createDate", createDate);
        doc.put("location", location);
        return doc;
    }

    private void assertDocsAreEqual(Map<String, Object> doc1, Map<String, Object> doc2) {
        assertEquals(getId(doc1), getId(doc2));
        assertEquals(doc1.get("name"), doc2.get("name"));
        assertEquals(doc1.get("title"), doc2.get("title"));
        assertEquals(doc1.get("caption"), doc2.get("caption"));
        assertEquals(doc1.get("createDate"), ElasticDateUtils.parseIso((String) doc2.get("createDate")));
        assertEquals(doc1.get("location"), doc2.get("location"));
    }

    private ResultFacet getFacet(SearchResult result, String name) {
        if (CollectionUtils.isNotEmpty(result.getFacets())) {
            for (final ResultFacet facet : result.getFacets()) {
                if (facet.getName().equals(name)) {
                    return facet;
                }
            }
        }
        return null;
    }

    @ComponentScan()
    static class Config {

        @Bean
        IndexPresetConfiguration indexPresetConfiguration() {
            final String indexAlias = "test_index";
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;
            return new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
        }

        @Bean
        List<FieldConfiguration> fieldConfiguration() {
            final List<FieldConfiguration> testFields = new ArrayList<>();
            testFields.add(createFieldConfiguration("id", ElasticsearchType.LONG, false, false, true, false));
            testFields.add(createFieldConfiguration("name", ElasticsearchType.TEXT, false, false, true, false));
            testFields.add(createFieldConfiguration("title", ElasticsearchType.TEXT, true, false, false, false));
            testFields.add(createFieldConfiguration("caption", ElasticsearchType.TEXT, true, false, false, false));
            testFields.add(createFieldConfiguration("createDate", ElasticsearchType.DATE, false, true, true, false));
            testFields.add(createFieldConfiguration("location", ElasticsearchType.TEXT, true, true, true, false));
            testFields.add(createFieldConfiguration("text_multilang", ElasticsearchType.TEXT, true, false, true, true));
            return testFields;
        }

        private FieldConfiguration createFieldConfiguration(String name,
                                                            ElasticsearchType elasticType,
                                                            boolean copyToFulltext,
                                                            boolean aggregatable,
                                                            boolean sortable,
                                                            boolean multilingual) {
            return StandardFieldConfiguration.builder(name, elasticType)
                    .copyToFulltext(copyToFulltext)
                    .aggregatable(aggregatable)
                    .sortable(sortable)
                    .multilingual(multilingual)
                    .build();
        }
    }
}
