package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.elasticsearch.config.DocumentBuilder;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.DayExpression;
import de.picturesafe.search.expression.DayRangeExpression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.RangeValueExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.AggregationField;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, ElasticsearchServiceIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class ElasticsearchServiceIT extends AbstractElasticsearchServiceIT {

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
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Katze"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Vögel"),
                SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(1, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Hamburg"),
                SearchParameter.builder().sortOptions(SortOption.desc("id")).build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4712, item.getId());
        assertDocsAreEqual(doc2, item.getAttributes());

        result = elasticsearchService.search(indexAlias, new FulltextExpression("Hamburg"),
                SearchParameter.builder().sortOptions(SortOption.asc("id")).build());
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getResultCount());
        item = result.getSearchResultItems().get(0);
        assertEquals(4711, item.getId());
        assertDocsAreEqual(doc1, item.getAttributes());
    }

    @Test
    public void testSearchRanges() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(4711).put("createDate", getDate("01.01.2020")).build(),
                DocumentBuilder.id(4712).put("createDate", getDate("01.05.2020")).build(),
                DocumentBuilder.id(4713).put("createDate", getDate("01.06.2020")).build(),
                DocumentBuilder.id(4714).put("createDate", getDate("31.12.2020")).build()
        ));

        SearchResult result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4714), SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4713), SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 4711, 4711), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("id", 1, 1000), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());

        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", getDate("01.01.2020"), getDate("31.12.2020")), SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", getDate("01.01.2020"), getDate("01.06.2020")), SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", getDate("01.01.2020"), getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", getDate("01.01.2019"), getDate("12.01.2019")), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new RangeValueExpression("createDate", getDate("01.01.2019"), getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
    }

    @Test
    public void testSearchDays() {
        indexName = elasticsearchService.createIndex(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.withoutId().put("createDate", getDate("01.01.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", getDate("01.05.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", getDate("01.06.2020")).build(),
                DocumentBuilder.withoutId().put("createDate", getDate("31.12.2020")).build()
        ));

        SearchResult result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("01.01.2020"), getDate("31.12.2020")), SearchParameter.DEFAULT);
        assertEquals(4, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("01.01.2020"), getDate("01.06.2020")), SearchParameter.DEFAULT);
        assertEquals(3, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("01.01.2020"), getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("01.01.2019"), getDate("12.01.2019")), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("01.01.2019"), getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayRangeExpression("createDate", getDate("31.12.2020"), getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());

        result = elasticsearchService.search(indexAlias, new DayExpression("createDate", getDate("01.01.2020")), SearchParameter.DEFAULT);
        assertEquals(1, result.getTotalHitCount());
        result = elasticsearchService.search(indexAlias, new DayExpression("createDate", getDate("02.01.2020")), SearchParameter.DEFAULT);
        assertEquals(0, result.getTotalHitCount());
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

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("name", "name"),
                SearchParameter.builder().aggregationFields(
                        new AggregationField("createDate", 1000), new AggregationField("location", 10)).build());
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

        final SearchResult result = elasticsearchService.search(indexAlias, new ValueExpression("title", "Multilang"),
                SearchParameter.builder().language("de").sortOptions(SortOption.asc("text_multilang")).build());
        assertEquals(4, result.getResultCount());
        assertEquals(4713, result.getSearchResultItems().get(0).getId());
        assertEquals(4712, result.getSearchResultItems().get(1).getId());
        assertEquals(4711, result.getSearchResultItems().get(2).getId());
        assertEquals(4714, result.getSearchResultItems().get(3).getId());
    }

    static class Config extends AbstractElasticsearchServiceIT.Config {

        @Bean
        List<FieldConfiguration> fieldConfiguration() {
            final List<FieldConfiguration> testFields = new ArrayList<>();
            testFields.add(FieldConfiguration.ID_FIELD);
            testFields.add(FieldConfiguration.FULLTEXT_FIELD);
            testFields.add(createFieldConfiguration("name", ElasticsearchType.TEXT, false, false, true, false));
            testFields.add(createFieldConfiguration("title", ElasticsearchType.TEXT, true, false, false, false));
            testFields.add(createFieldConfiguration("caption", ElasticsearchType.TEXT, true, false, false, false));
            testFields.add(createFieldConfiguration("createDate", ElasticsearchType.DATE, false, true, true, false));
            testFields.add(createFieldConfiguration("location", ElasticsearchType.TEXT, true, true, true, false));
            testFields.add(createFieldConfiguration("text_multilang", ElasticsearchType.TEXT, true, false, true, true));
            return testFields;
        }
    }

    private Date getDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Parsing date '" + date + "' failed!", e);
        }
    }
}
