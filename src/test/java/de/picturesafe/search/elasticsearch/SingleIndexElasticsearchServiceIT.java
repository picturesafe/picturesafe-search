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
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.AccountContext;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.spring.configuration.TestConfiguration;
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

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, SingleIndexElasticsearchServiceIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class SingleIndexElasticsearchServiceIT {

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    @Autowired
    RestClientConfiguration elasticsearchRestClientConfiguration;

    private RestHighLevelClient restClient;
    private String indexName;

    @Before
    public final void setup() {
        this.restClient = elasticsearchRestClientConfiguration.getClient();
        indexName = null;
    }

    @After
    public void cleanup() {
        if (singleIndexElasticsearchService.getElasticsearchService().aliasExists(singleIndexElasticsearchService.getIndexAlias())) {
            singleIndexElasticsearchService.getElasticsearchService().removeAlias(singleIndexElasticsearchService.getIndexAlias());
        }
        if (indexName != null) {
            singleIndexElasticsearchService.getElasticsearchService().deleteIndex(indexName);
        }
    }

    @Test
    public void testCreateIndexWithAlias() {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        assertTrue(singleIndexElasticsearchService.getElasticsearchService().aliasExists(singleIndexElasticsearchService.getIndexAlias()));
    }

    @Test
    public void testAddFieldConfiguration() throws IOException {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();

        final String fieldName = "add_field_test";
        singleIndexElasticsearchService.addFieldConfiguration(StandardFieldConfiguration.builder(fieldName, ElasticsearchType.TEXT).copyToFulltext(true).build());

        final GetMappingsResponse response = restClient.indices().getMapping(new GetMappingsRequest().indices(indexName), RequestOptions.DEFAULT);
        final MappingMetaData mapping = response.mappings().get(indexName);
        final Map<String, Object> properties = (Map<String, Object>) mapping.sourceAsMap().get("properties");
        assertTrue("Mapping should contain new field", properties.containsKey(fieldName));
    }

    @Test
    public void testIndexNames() {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        assertEquals(indexName, singleIndexElasticsearchService.getIndexName());
    }

    @Test
    public void testIndexVersion() {
        final int indexVersion = 4711;
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        singleIndexElasticsearchService.setIndexVersion(indexVersion);
        assertEquals(indexVersion, singleIndexElasticsearchService.getIndexVersion());
    }

    @Test
    public void testAddToIndex() throws Exception {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        final Map<String, Object> doc = createDocument(System.currentTimeMillis());
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, doc);

        final GetResponse response = restClient.get(new GetRequest(singleIndexElasticsearchService.getIndexAlias()).id(String.valueOf(getId(doc))),
                RequestOptions.DEFAULT);
        assertTrue("Document added cannot be found!", response.isExists());
        assertDocsAreEqual(doc, response.getSource());
    }

    @Test
    public void testAddToIndexMultiple() throws Exception {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        final Map<String, Object> doc1 = createDocument(1);
        final Map<String, Object> doc2 = createDocument(2);
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        GetResponse response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        assertDocsAreEqual(doc1, response.getSource());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());
        assertDocsAreEqual(doc2, response.getSource());
    }

    @Test
    public void testRemoveFromIndex() throws Exception {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        final Map<String, Object> doc1 = createDocument(1);
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, doc1);
        final Map<String, Object> doc2 = createDocument(2);
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, doc2);

        GetResponse response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());

        singleIndexElasticsearchService.removeFromIndex(DataChangeProcessingMode.BLOCKING, 1);
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("1"), RequestOptions.DEFAULT);
        assertFalse("Document #1 should be deleted!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 should still exist!", response.isExists());
    }

    @Test
    public void testRemoveFromIndexMultiple() throws Exception {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        final Map<String, Object> doc1 = createDocument(1);
        final Map<String, Object> doc2 = createDocument(2);
        final Map<String, Object> doc3 = createDocument(3);
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3));

        GetResponse response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("1"), RequestOptions.DEFAULT);
        assertTrue("Document #1 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 cannot be found!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("3"), RequestOptions.DEFAULT);
        assertTrue("Document #3 cannot be found!", response.isExists());

        singleIndexElasticsearchService.removeFromIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(1L, 3L));
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("1"), RequestOptions.DEFAULT);
        assertFalse("Document #1 should be deleted!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("2"), RequestOptions.DEFAULT);
        assertTrue("Document #2 should still exist!", response.isExists());
        response = restClient.get(new GetRequest().index(singleIndexElasticsearchService.getIndexAlias()).id("3"), RequestOptions.DEFAULT);
        assertFalse("Document #3 should be deleted!", response.isExists());
    }

    @Test
    public void testSearchSimple() {
        indexName = singleIndexElasticsearchService.createIndexWithAlias();
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg");
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        SearchResult result = singleIndexElasticsearchService.search(new AccountContext(), new ValueExpression("title", "Hund"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        SearchResultItem item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());

        result = singleIndexElasticsearchService.search(new AccountContext(), new ValueExpression("title", "Katze"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = singleIndexElasticsearchService.search(new AccountContext(), new FulltextExpression("Vögel"),
                new SearchParameter());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = singleIndexElasticsearchService.search(new AccountContext(), new FulltextExpression("Hamburg"),
                new SearchParameter(new SortOption("id", SortOption.Direction.DESC)));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = singleIndexElasticsearchService.search(new AccountContext(), new FulltextExpression("Hamburg"),
                new SearchParameter(new SortOption("id", SortOption.Direction.ASC)));
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());
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
