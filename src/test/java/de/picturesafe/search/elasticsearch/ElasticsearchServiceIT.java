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
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.IndexObject;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.DayExpression;
import de.picturesafe.search.expression.DayRangeExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FindAllExpression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.KeywordExpression;
import de.picturesafe.search.expression.OperationExpression;
import de.picturesafe.search.expression.RangeValueExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.CollapseOption;
import de.picturesafe.search.parameter.InnerHitsOption;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.parameter.aggregation.DateHistogramAggregation;
import de.picturesafe.search.parameter.aggregation.DefaultAggregation;
import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDate;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;
import static de.picturesafe.search.parameter.aggregation.DateHistogramAggregation.IntervalType.CALENDAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, ElasticsearchServiceIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class ElasticsearchServiceIT extends AbstractElasticsearchServiceIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceIT.class);

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
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
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

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Vögel"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId(Long.class).longValue());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Hamburg"),
                SearchParameter.builder().sortOptions(SortOption.desc("id")).build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId(Long.class).longValue());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Hamburg"),
                SearchParameter.builder().sortOptions(SortOption.asc("id")).build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId(Long.class).longValue());
        assertDocsAreEqual(doc1, item.getAttributes());
    }

    @Test
    public void testSearchComplex() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.id(9001).put("title", "Titel 1").put("count", 13).build(),
                DocumentBuilder.id(9002).put("title", "Titel 2").put("count", 27).build(),
                DocumentBuilder.id(9003).put("title", "Titel 3").put("count", 42).build()
        );
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, docs);
        final OperationExpression andExpression = OperationExpression.and(
                new FulltextExpression("titel"),
                new ValueExpression("count", 27));
        SearchResult result = elasticsearchService.search(indexAlias, andExpression, SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());

        result = elasticsearchService.search(indexAlias, andExpression, SearchParameter.builder().sortOptions(SortOption.relevance()).build());
        assertEquals(1, result.getTotalHitCount());
    }

    @Test
    public void testSearchRanges() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(4711).put("createDate", parseDate("01.01.2020")).build(),
                DocumentBuilder.id(4712).put("createDate", parseDate("01.05.2020")).build(),
                DocumentBuilder.id(4713).put("createDate", parseDate("01.06.2020")).build(),
                DocumentBuilder.id(4714).put("createDate", parseDate("31.12.2020")).build()
        ));

        SearchResult result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4714), SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4713), SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4711), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 1, 1000), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());

        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", parseDate("01.01.2020"), parseDate("31.12.2020")),
                SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", parseDate("01.01.2020"), parseDate("01.06.2020")),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", parseDate("01.01.2020"), parseDate("01.01.2020")),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", parseDate("01.01.2019"), parseDate("12.01.2019")),
                SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", parseDate("01.01.2019"), parseDate("01.01.2020")),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
    }

    @Test
    public void testSearchDays() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.withoutId().put("createDate", parseDate("01.01.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", parseDate("01.05.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", parseDate("01.06.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", parseDate("31.12.2020")).build()
        ));

        SearchResult result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("01.01.2020"), parseDate("31.12.2020")),
                SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("01.01.2020"), parseDate("01.06.2020")),
                SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("01.01.2020"), parseDate("01.01.2020")),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("01.01.2019"), parseDate("12.01.2019")),
                SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("01.01.2019"), parseDate("01.01.2020")),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", parseDate("31.12.2020"), parseDate("01.01.2020")),
                SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());

        result = elasticsearchService.search(indexAlias, new DayExpression("createDate", parseDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayExpression("createDate", parseDate("02.01.2020")), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
    }

    @Test
    public void testSearchAggregations() throws Exception {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg",
                DateUtils.parseDate("10.05.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg",
                DateUtils.parseDate("01.05.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc3 = createDocument(4713, "Die Maus versteckt sich vor der Katze in Hamburg",
                DateUtils.parseDate("01.01.2019", "dd.MM.yyyy"), "Hamburg");
        final Map<String, Object> doc4 = createDocument(4714, "Die Entwickler feiert Geburtstag in Buchholz",
                DateUtils.parseDate("28.07.2018", "dd.MM.yyyy"), "Buchholz");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        final SearchResult result = elasticsearchService.search(indexAlias, new FindAllExpression(),
                SearchParameter.builder().aggregations(
                        DefaultAggregation.field("location"),
                        DateHistogramAggregation.field("createDate").interval(CALENDAR, "1y").format("yyyy").name("years")
                ).build());
        assertEquals(4, result.getTotalHitCount());
        assertEquals(4, result.getResultCount());
        assertEquals(2, result.getFacets().size());

        final ResultFacet locationFacet = getFacet(result, "location");
        assertNotNull(locationFacet);
        assertEquals("location", locationFacet.getFieldName());
        assertEquals(4, locationFacet.getCount());
        ResultFacetItem facetItem = locationFacet.getFacetItems().get(0);
        assertEquals("Hamburg", facetItem.getValue());
        assertEquals(3, facetItem.getCount());
        facetItem = locationFacet.getFacetItems().get(1);
        assertEquals("Buchholz", facetItem.getValue());
        assertEquals(1, facetItem.getCount());

        final ResultFacet yearsFacet = getFacet(result, "years");
        assertNotNull(yearsFacet);
        assertEquals("createDate", yearsFacet.getFieldName());
        assertEquals(4, yearsFacet.getCount());
        final Map<String, ResultFacetItem> years = yearsFacet.getFacetItems().stream().collect(Collectors.toMap(f -> f.getValue().toString(), f -> f));
        final ResultFacetItem year2019 = years.get("2019");
        assertEquals(3, year2019.getCount());
        final ResultFacetItem year2018 = years.get("2018");
        assertEquals(1, year2018.getCount());
    }

    @Test
    public void testSearchMultilang() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        final Map<String, Object> doc1 = createDocument(4711, "Der Hund beißt sich in den Schwanz in Hamburg");
        doc1.put("text_multilang.de", "Dies ist ein deutscher Text.");
        doc1.put("text_multilang.en", "This is an english text.");
        final Map<String, Object> doc2 = createDocument(4712, "Die Katze jagt Vögel in Hamburg");
        doc2.put("text_multilang.de", "Ich spreche Deutsch.");
        doc2.put("text_multilang.en", "I speak english.");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2));

        SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("text_multilang", "deutsch"),
                SearchParameter.builder().language("de").build());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new ValueExpression("text_multilang", "deutsch"),
                SearchParameter.builder().language("en").build());
        assertEquals(0, result.getTotalHitCount());
        assertEquals(0, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new ValueExpression("text_multilang", "english"),
                SearchParameter.builder().language("en").build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("deutscher"),
                SearchParameter.builder().language("de").build());
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("english"),
                SearchParameter.builder().language("en").build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
    }

    @Test
    public void testSortMultilang() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        final Map<String, Object> doc1 = createDocument(4711, "Äöü Multilang-Test 1");
        doc1.put("text_multilang.de", "Äöü Test");
        final Map<String, Object> doc2 = createDocument(4712, "Aou Multilang-Test 2");
        doc2.put("text_multilang.de", "Aou Test");
        final Map<String, Object> doc3 = createDocument(4713, "Äöü Multilang-Best 3");
        doc3.put("text_multilang.de", "Äöü Best");
        final Map<String, Object> doc4 = createDocument(4714, "Bei Multilang-Test 4");
        doc4.put("text_multilang.de", "Bei Test");
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(doc1, doc2, doc3, doc4));

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Multilang"),
                SearchParameter.builder().language("de").sortOptions(SortOption.asc("text_multilang")).build());
        assertEquals(4, result.getResultCount());
        assertEquals(4713, result.getSearchResultItems().get(0).getId(Long.class).longValue());
        assertEquals(4712, result.getSearchResultItems().get(1).getId(Long.class).longValue());
        assertEquals(4711, result.getSearchResultItems().get(2).getId(Long.class).longValue());
        assertEquals(4714, result.getSearchResultItems().get(3).getId(Long.class).longValue());
    }

    @Test
    public void testSearchWithBoost() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(7001).put("name", "aaa").put("title", "bbb").build(),
                DocumentBuilder.id(7002).put("name", "bbb").put("title", "aaa").build(),
                DocumentBuilder.id(7003).put("name", "aaa").build(),
                DocumentBuilder.id(7004).put("name", "bbb").build(),
                DocumentBuilder.id(7005).put("name", "ccc").build()
        ));

        Expression expression = OperationExpression.or(new ValueExpression("name", "aaa"), new ValueExpression("title", "aaa"));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.relevance(), SortOption.asc("id")).build();
        SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getResultCount());
        result.forEach(item -> LOGGER.debug("{}", item));
        assertEquals(7001, result.getSearchResultItem(0).getId(Integer.class).intValue());
        assertEquals(7003, result.getSearchResultItem(1).getId(Integer.class).intValue());
        assertEquals(7002, result.getSearchResultItem(2).getId(Integer.class).intValue());

        expression = OperationExpression.or(new ValueExpression("title", "aaa").boost(2.0F), new ValueExpression("name", "aaa"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getResultCount());
        assertEquals(7002, result.getSearchResultItem(0).getId(Integer.class).intValue());
        assertEquals(7001, result.getSearchResultItem(1).getId(Integer.class).intValue());
        assertEquals(7003, result.getSearchResultItem(2).getId(Integer.class).intValue());

        expression = OperationExpression.or(new FulltextExpression("aaa").boost(2.0F), new ValueExpression("name", "aaa"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getResultCount());
        assertEquals(7002, result.getSearchResultItem(0).getId(Integer.class).intValue());
        assertEquals(7001, result.getSearchResultItem(1).getId(Integer.class).intValue());
        assertEquals(7003, result.getSearchResultItem(2).getId(Integer.class).intValue());

        expression = OperationExpression.or(new KeywordExpression("name", "aaa").boost(2.0F), new ValueExpression("title", "aaa"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getResultCount());
        assertEquals(7001, result.getSearchResultItem(0).getId(Integer.class).intValue());
        assertEquals(7003, result.getSearchResultItem(1).getId(Integer.class).intValue());
        assertEquals(7002, result.getSearchResultItem(2).getId(Integer.class).intValue());

        expression = OperationExpression.or(new InExpression("name", "aaa", "ccc").boost(2.0F), new ValueExpression("title", "aaa"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(4, result.getResultCount());
        assertEquals(7001, result.getSearchResultItem(0).getId(Integer.class).intValue());
        assertEquals(7003, result.getSearchResultItem(1).getId(Integer.class).intValue());
        assertEquals(7005, result.getSearchResultItem(2).getId(Integer.class).intValue());
        assertEquals(7002, result.getSearchResultItem(3).getId(Integer.class).intValue());
    }

    @Test
    public void testAddGetIndexObject() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);

        final TestObject obj1 = new TestObject(666, "TestObject 1", parseDate("18.03.2020"));
        elasticsearchService.addObjectToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, obj1);
        TestObject indexObject = elasticsearchService.getObject(indexAlias, obj1.id, TestObject.class);
        assertEquals(obj1, indexObject);

        final TestObject obj2 = new TestObject(667, "TestObject 2", parseDate("19.03.2020"));
        final TestObject obj3 = new TestObject(668, "TestObject 3", parseDate("20.03.2020"));
        elasticsearchService.addObjectsToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(obj2, obj3));
        indexObject = elasticsearchService.getObject(indexAlias, obj2.id, TestObject.class);
        assertEquals(obj2, indexObject);
        indexObject = elasticsearchService.getObject(indexAlias, obj3.id, TestObject.class);
        assertEquals(obj3, indexObject);
    }

    @Test
    public void testPersistingIndexConfig() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);

        final IndexPresetConfiguration indexConfig
                = new StandardIndexPresetConfiguration("test-persistence", "test-persistence-prefix", "yyyy/MM/dd-HH/mm/ss", 17, 23, 123456);
        elasticsearchService.addObjectToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, indexConfig, 12345);
        final IndexPresetConfiguration storedConfig = elasticsearchService.getObject(indexAlias, 12345, IndexPresetConfiguration.class);
        assertEquals(indexConfig, storedConfig);
    }

    @Test
    public void testPersistingFieldConfig() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);

        FieldConfiguration fieldConfig = StandardFieldConfiguration.builder("test-persistence", ElasticsearchType.TEXT)
                .sortable(true).aggregatable(false).multilingual(true).analyzer("myAnalyzer").copyToFulltext(true).copyTo("test").build();
        elasticsearchService.addObjectToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, fieldConfig, 1001);
        FieldConfiguration storedConfig = elasticsearchService.getObject(indexAlias, 1001, FieldConfiguration.class);
        assertEquals(fieldConfig, storedConfig);

        fieldConfig = StandardFieldConfiguration.builder("test-nested-persistence", ElasticsearchType.NESTED).innerFields(
                StandardFieldConfiguration.builder("test-persistence-1", ElasticsearchType.TEXT)
                        .sortable(true).aggregatable(false).multilingual(true).analyzer("myAnalyzer").copyToFulltext(true).copyTo("test").build(),
                StandardFieldConfiguration.builder("test-persistence-2", ElasticsearchType.INTEGER).sortable(true).aggregatable(true).build())
        .build();
        elasticsearchService.addObjectToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, fieldConfig, 1002);
        storedConfig = elasticsearchService.getObject(indexAlias, 1002, FieldConfiguration.class);
        assertEquals(fieldConfig, storedConfig);
    }

    @Test
    public void testSortByRelevance() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(2001).put("title", "Black horse")
                        .put("caption", "I was riding on a black horse long time ago.").build(),
                DocumentBuilder.id(2002).put("title", "White horse")
                        .put("caption", "I was riding on a white horse followed by a black dog.").build(),
                DocumentBuilder.id(2003).put("title", "Black and white")
                        .put("caption", "Michael Jackson was singing about black and white, not about a black horse.").build()
        ));

        final Expression expression = new FulltextExpression("black");
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.relevance()).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getTotalHitCount());
        assertEquals(2003, result.getSearchResultItems().get(0).getId(Long.class).longValue());
        assertEquals(2001, result.getSearchResultItems().get(1).getId(Long.class).longValue());
        assertEquals(2002, result.getSearchResultItems().get(2).getId(Long.class).longValue());
    }

    @Test
    public void testCollapseWithInnerHits() {
        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.id(10).put("title", "collapse").put("keyword", "Elastic").put("count", 1).build(),
                DocumentBuilder.id(11).put("title", "collapse").put("keyword", "Elastic").put("count", 2).build(),
                DocumentBuilder.id(13).put("title", "collapse").put("keyword", "Elastic").put("count", 3).build(),
                DocumentBuilder.id(21).put("title", "collapse").put("keyword", "Java").put("count", 11).build(),
                DocumentBuilder.id(22).put("title", "collapse").put("keyword", "Java").put("count", 12).build()
        );
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, docs);

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("title", "collapse"),
                SearchParameter.builder().sortOptions(SortOption.desc("count")).collapseOption(CollapseOption.field("keyword")
                        .innerHits(InnerHitsOption.name("maxCounts").size(2).sortOptions(SortOption.desc("count")))).build());
        assertEquals(2, result.getSearchResultItems().size());

        SearchResultItem searchResultItem = result.getSearchResultItems().get(0);
        assertEquals("Java", searchResultItem.getAttribute("keyword"));
        assertNotNull(searchResultItem.getInnerHits());
        assertEquals(2, searchResultItem.getInnerHits().get("maxCounts").size());
        SearchResultItem innerHit = searchResultItem.getInnerHits().get("maxCounts").get(0);
        assertEquals(12, innerHit.getAttribute("count"));
        innerHit = searchResultItem.getInnerHits().get("maxCounts").get(1);
        assertEquals(11, innerHit.getAttribute("count"));

        searchResultItem = result.getSearchResultItems().get(1);
        assertEquals("Elastic", searchResultItem.getAttribute("keyword"));
        assertNotNull(searchResultItem.getInnerHits());
        assertEquals(2, searchResultItem.getInnerHits().get("maxCounts").size());
        innerHit = searchResultItem.getInnerHits().get("maxCounts").get(0);
        assertEquals(3, innerHit.getAttribute("count"));
        innerHit = searchResultItem.getInnerHits().get("maxCounts").get(1);
        assertEquals(2, innerHit.getAttribute("count"));
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Parsing date '" + date + "' failed!", e);
        }
    }

    public static class TestObject implements IndexObject<TestObject> {

        private long id;
        private String title;
        private Date createDate;

        public TestObject() {
        }

        public TestObject(long id, String title, Date createDate) {
            this.id = id;
            this.title = title;
            this.createDate = createDate;
        }

        @Override
        public Map<String, Object> toDocument() {
            return DocumentBuilder.id(id).put("title", title).put("createDate", createDate).build();
        }

        @Override
        public TestObject fromDocument(Map<String, Object> document) {
            id = getId(document, 0);
            title = getString(document, "title");
            createDate = getDate(document, "createDate");
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TestObject that = (TestObject) o;
            return new EqualsBuilder().append(id, that.id).append(title, that.title).append(createDate, that.createDate).isEquals();
        }

        @Override
        public int hashCode() {
            return (int) id;
        }
    }

    static class Config extends AbstractElasticsearchServiceIT.Config {

        @Bean
        List<FieldConfiguration> fieldConfiguration() {
            return Arrays.asList(
                    FieldConfiguration.ID_FIELD,
                    FieldConfiguration.FULLTEXT_FIELD,
                    createFieldConfiguration("name", ElasticsearchType.TEXT, false, false, true, false),
                    createFieldConfiguration("title", ElasticsearchType.TEXT, true, false, false, false),
                    createFieldConfiguration("caption", ElasticsearchType.TEXT, true, false, false, false),
                    createFieldConfiguration("createDate", ElasticsearchType.DATE, false, true, true, false),
                    createFieldConfiguration("location", ElasticsearchType.TEXT, true, true, true, false),
                    createFieldConfiguration("text_multilang", ElasticsearchType.TEXT, true, false, true, true),
                    createFieldConfiguration("keyword", ElasticsearchType.KEYWORD, false, false, true, false),
                    createFieldConfiguration("count", ElasticsearchType.INTEGER, false, false, true, false)
            );
        }
    }
}
