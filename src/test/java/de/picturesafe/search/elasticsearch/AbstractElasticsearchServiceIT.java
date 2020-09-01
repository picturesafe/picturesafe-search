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
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.error.QuerySyntaxException;
import de.picturesafe.search.elasticsearch.impl.StaticIndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.Version;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

abstract class AbstractElasticsearchServiceIT {

    @Autowired
    protected IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    protected ElasticsearchService elasticsearchService;

    @Autowired
    protected RestClientConfiguration elasticsearchRestClientConfiguration;

    protected RestHighLevelClient restClient;
    protected String indexName;
    protected String indexAlias;

    protected void doSetup() {
        indexAlias = indexPresetConfiguration.getIndexAlias();
        restClient = elasticsearchRestClientConfiguration.getClient();
        indexName = null;
    }

    protected void doCleanup() {
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
    @SuppressWarnings("unchecked")
    public void testAddFieldConfiguration() throws IOException {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);

        final String fieldName = "add_field_test";
        elasticsearchService.addFieldConfiguration(indexAlias, StandardFieldConfiguration.builder(fieldName, ElasticsearchType.TEXT).copyToFulltext(true)
                .build());

        final GetMappingsResponse response = restClient.indices().getMapping(new GetMappingsRequest().indices(indexName), RequestOptions.DEFAULT);
        final MappingMetadata mapping = response.mappings().get(indexName);
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
    public void testQuerySyntaxException() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, doc);

        final SearchResult result = elasticsearchService.search(indexAlias,
                new ValueExpression("title", "Hund"), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());

        try {
            elasticsearchService.search(indexAlias,
                    new ValueExpression("title", "/"), SearchParameter.DEFAULT);
            fail("Expected an QuerySyntaxException to be thrown!");
        } catch (QuerySyntaxException qse) {
            assertEquals("/", qse.getInvalidQueryString());
        }

        try {
            elasticsearchService.search(indexAlias,
                    new ValueExpression("title", "(Hund OR Schwanz) AND (Hamburg"), SearchParameter.DEFAULT);
            fail("Expected an QuerySyntaxException to be thrown!");
        } catch (QuerySyntaxException qse) {
            assertEquals("(Hund OR Schwanz) AND (Hamburg", qse.getInvalidQueryString());
        }
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

        SearchResult result = elasticsearchService.search(indexAlias,
                new InExpression("id", 4711, 4714),
                SearchParameter.DEFAULT);
        assertEquals(2, result.getResultCount());

        result = elasticsearchService.search(indexAlias,
                new InExpression("name", "name-4711", "name-4713", "name-4714"),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getResultCount());

        result = elasticsearchService.search(indexAlias,
                new InExpression("id", 4711L, 4712L, 4714L),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getResultCount());

        result = elasticsearchService.search(indexAlias,
                new InExpression("id", new long[] {4711}),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId(Long.class).longValue());

        result = elasticsearchService.search(indexAlias,
                new InExpression("name", "name-4711"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId(Long.class).longValue());

        result = elasticsearchService.search(indexAlias,
                new InExpression("name", "name-4711", "name-4714"),
                SearchParameter.DEFAULT);
        assertEquals(2, result.getResultCount());
        assertEquals(4711, result.getSearchResultItems().get(0).getId(Long.class).longValue());
        assertEquals(4714, result.getSearchResultItems().get(1).getId(Long.class).longValue());
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

        SearchResult result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("id", 4711, 4712, 4714)),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getResultCount());
        assertEquals(4713, result.getSearchResultItems().get(0).getId(Long.class).longValue());

        result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("name", "name-4711", "name-4713", "name-4714")),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId(Long.class).longValue());

        result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("id", 4711L, 4712L, 4714L)),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("id", new long[] {4711})),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId(Long.class).longValue());
        assertEquals(4713, result.getSearchResultItems().get(1).getId(Long.class).longValue());
        assertEquals(4714, result.getSearchResultItems().get(2).getId(Long.class).longValue());

        result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("name", "name-4711")),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getResultCount());

        result = elasticsearchService.search(indexAlias,
                new MustNotExpression(new InExpression("name", "name-4711", "name-4714")),
                SearchParameter.DEFAULT);
        assertEquals(2, result.getResultCount());
        assertEquals(4712, result.getSearchResultItems().get(0).getId(Long.class).longValue());
        assertEquals(4713, result.getSearchResultItems().get(1).getId(Long.class).longValue());
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

        final SearchParameter searchParameter = SearchParameter.builder().maxResults(1).maxTrackTotalHits(2L).build();
        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Hund"),
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

    protected Map<String, Object> createDocument(long id) {
        return createDocument(id, "Document title #" + id);
    }

    protected Map<String, Object> createDocument(long id, String title) {
        return createDocument(id, title, new Date(), "Hamburg");
    }

    protected Map<String, Object> createDocument(long id, String title, Date createDate, String location) {
        return DocumentBuilder.id(id)
                .put("name", "name-" + id)
                .put("title", title)
                .put("caption", "Document caption #" + id + "\nThis document was created for testing purposes.\nÄÖÜäöüß")
                .put("createDate", createDate)
                .put("location", location)
                .build();
    }

    protected void assertDocsAreEqual(Map<String, Object> doc1, Map<String, Object> doc2) {
        assertEquals(getId(doc1), getId(doc2));
        assertEquals(doc1.get("name"), doc2.get("name"));
        assertEquals(doc1.get("title"), doc2.get("title"));
        assertEquals(doc1.get("caption"), doc2.get("caption"));
        assertEquals(doc1.get("createDate"), doc2.get("createDate"));
        assertEquals(doc1.get("location"), doc2.get("location"));
    }

    protected ResultFacet getFacet(SearchResult result, String name) {
        if (CollectionUtils.isNotEmpty(result.getFacets())) {
            for (final ResultFacet facet : result.getFacets()) {
                if (facet.getName().equals(name)) {
                    return facet;
                }
            }
        }
        return null;
    }

    @Configuration
    @ComponentScan(basePackages = "de.picturesafe.search.elasticsearch.connect")
    protected static class Config {

        @Bean
        IndexPresetConfiguration indexPresetConfiguration() {
            final String indexAlias = "test_index";
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;
            return new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
        }

        @Bean
        IndexPresetConfigurationProvider indexPresetConfigurationProvider(IndexPresetConfiguration indexPresetConfiguration) {
            return new StaticIndexPresetConfigurationProvider(indexPresetConfiguration);
        }

        protected FieldConfiguration createFieldConfiguration(String name,
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
