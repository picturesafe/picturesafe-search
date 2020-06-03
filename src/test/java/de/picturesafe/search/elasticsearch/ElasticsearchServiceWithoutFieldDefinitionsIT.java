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

import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import de.picturesafe.search.spring.configuration.DefaultElasticConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DefaultElasticConfiguration.class, AbstractElasticsearchServiceIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class ElasticsearchServiceWithoutFieldDefinitionsIT extends AbstractElasticsearchServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceWithoutFieldDefinitionsIT.class);

    @Before
    public void setup() {
        doSetup();
    }

    @After
    public void cleanup() {
        doCleanup();
    }

    @Test
    public void testSearchSimple() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Hund"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        SearchResultItem item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId(Long.class).longValue());
        assertDocsAreEqual(doc1, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Katze"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId(Long.class).longValue());
        assertDocsAreEqual(doc2, item.getAttributes());
    }

    @Test
    public void testSearchFacets() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.id(111).put("name", "name-111").put("group", 1).put("special", false).build(),
                DocumentBuilder.id(112).put("name", "name-112").put("group", 1).put("special", false).build(),
                DocumentBuilder.id(113).put("name", "name-113").put("group", 1).put("special", false).build(),
                DocumentBuilder.id(114).put("name", "name-114").put("group", 2).put("special", true).build(),
                DocumentBuilder.id(115).put("name", "name-115").put("group", 2).put("special", false).build());
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, docs);

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("name", "name"),
                SearchParameter.builder().aggregations(
                        TermsAggregation.field("group").maxCount(1000), TermsAggregation.field("special").maxCount(10)).build());
        assertEquals(5, result.getTotalHitCount());
        assertEquals(5, result.getResultCount());
        assertEquals(2, result.getFacets().size());

        final ResultFacet groupFacet = getFacet(result, "group");
        assertNotNull(groupFacet);
        assertEquals(5, groupFacet.getCount());
        ResultFacetItem facetItem = groupFacet.getFacetItems().get(0);
        assertEquals("1", facetItem.getValue());
        assertEquals(3, facetItem.getCount());
        facetItem = groupFacet.getFacetItems().get(1);
        assertEquals("2", facetItem.getValue());
        assertEquals(2, facetItem.getCount());

        final ResultFacet specialFacet = getFacet(result, "special");
        assertNotNull(specialFacet);
        assertEquals(5, specialFacet.getCount());
        facetItem = specialFacet.getFacetItems().get(0);
        assertEquals("false", facetItem.getValue());
        assertEquals(4, facetItem.getCount());
        facetItem = specialFacet.getFacetItems().get(1);
        assertEquals("true", facetItem.getValue());
        assertEquals(1, facetItem.getCount());
    }

    @Test
    public void testWithoutId() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.withoutId().put("name", "name-1").put("ordinal", 1).build(),
                DocumentBuilder.withoutId().put("name", "name-2").put("ordinal", 2).build(),
                DocumentBuilder.withoutId().put("name", "name-3").put("ordinal", 3).build());
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, docs);

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("name", "name"),
                SearchParameter.builder().sortOptions(SortOption.asc("ordinal")).build());
        assertEquals(3, result.getTotalHitCount());
        assertEquals(3, result.getResultCount());

        int idx = 0;
        for (final SearchResultItem item : result.getSearchResultItems()) {
            LOGGER.debug("{}", item);
            assertEquals("name-" + (idx + 1), item.getAttribute("name"));
            assertNotNull(item.getId());
            assertEquals("name-" + (idx + 1), elasticsearchService.getDocument(indexAlias, item.getId()).get("name"));
            idx++;
        };
    }
}
