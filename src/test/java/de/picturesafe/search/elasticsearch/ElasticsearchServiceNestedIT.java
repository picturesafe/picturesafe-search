package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.DayExpression;
import de.picturesafe.search.expression.DayRangeExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.IsNullExpression;
import de.picturesafe.search.expression.KeywordExpression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.expression.RangeValueExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static de.picturesafe.search.elasticsearch.config.FieldConfiguration.FIELD_NAME_ID;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class, ElasticsearchServiceNestedIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class ElasticsearchServiceNestedIT {

    @Autowired
    protected IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    protected ElasticsearchService elasticsearchService;

    @Autowired
    protected RestClientConfiguration elasticsearchRestClientConfiguration;

    protected RestHighLevelClient restClient;
    protected String indexName;
    protected String indexAlias;

    @Before
    public void setup() {
        indexAlias = indexPresetConfiguration.getIndexAlias();
        restClient = elasticsearchRestClientConfiguration.getClient();

        indexName = elasticsearchService.createIndexWithAlias(indexAlias);
        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(1)
                        .put("article", Collections.singletonList(
                                DocumentBuilder.id(1001)
                                .put("title", "This is a test title")
                                .put("rubric", "News")
                                .put("author", "John Doe")
                                .put("page", 1)
                                .put("date", parseDate("27.03.2020")).build()
                        )).build(),
                DocumentBuilder.id(2)
                        .put("article", Arrays.asList(
                                DocumentBuilder.id(1002)
                                .put("title", "This is another test title")
                                .put("rubric", "News")
                                .put("author", "Jane Doe")
                                .put("page", 1)
                                .put("date", parseDate("26.03.2020")).build(),
                                DocumentBuilder.id(1003)
                                .put("title", "This also is another test title")
                                .put("rubric", "Politics")
                                .put("author", "Jeanne d’Arc")
                                .put("page", 2)
                                .put("date", parseDate("27.03.2020")).build()
                        )).build(),
                DocumentBuilder.id(3)
                        .put("article", Collections.singletonList(
                                DocumentBuilder.id(1004)
                                .put("title", "This is one more test title")
                                .put("rubric", (String) null)
                                .put("author", "Jane Doe")
                                .put("page", 13)
                                .put("date", parseDate("01.03.2020")).build()
                        )).build()
        ));
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
    public void testValueExpression() {
        Expression expression = new ValueExpression("article.author", "Jane Doe");
        SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("article.id")).build();
        SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());

        searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.page")).build();
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());
        assertEquals(3, result.getSearchResultItems().get(1).getId());

        searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("article.rubric")).build();
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());
        assertEquals(3, result.getSearchResultItems().get(1).getId());

        searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.date")).build();
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());

        expression = new ValueExpression("article.title", "another test");
        searchParameter = SearchParameter.DEFAULT;
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());

        expression = new ValueExpression("article.id", 1003);
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());

        expression = new ValueExpression("article.date", parseDate("01.03.2020"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testFulltextExpression() {
        final Expression expression = new FulltextExpression("another test");
        final SearchParameter searchParameter = SearchParameter.DEFAULT;
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testDayExpression() {
        Expression expression = new DayExpression("article.date", parseDate("01.03.2020"));
        SearchParameter searchParameter = SearchParameter.DEFAULT;
        SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());

        expression = new DayExpression("article.date", DayExpression.Comparison.GE, parseDate("01.03.2020"));
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(3, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
        assertEquals(3, result.getSearchResultItems().get(2).getId());

        expression = new DayExpression("article.date", DayExpression.Comparison.GT, parseDate("01.03.2020"));
        searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testDayRangeExpression() {
        final Expression expression = new DayRangeExpression("article.date", parseDate("21.03.2020"), parseDate("31.03.2020"));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testInExpression() {
        final Expression expression = new InExpression("article.page", 1, 2, 3);
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testIsNullExpression() {
        final Expression expression = new IsNullExpression("article.rubric");
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testKeywordExpression() {
        final Expression expression = new KeywordExpression("article.rubric", "Politics");
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(2, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testRangeValueExpression() {
        final Expression expression = new RangeValueExpression("article.page", 1, 3);
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testMustNotValueExpression() {
        final Expression expression = new MustNotExpression(new ValueExpression("article.page", 13));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testMustNotFulltextExpression() {
        final Expression expression = new MustNotExpression(new FulltextExpression("another"));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(3, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testMustNotDayExpression() {
        final Expression expression = new MustNotExpression(new DayExpression("article.date", parseDate("01.03.2020")));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    @Test
    public void testMustNotDayRangeExpression() {
        final Expression expression = new MustNotExpression(new DayRangeExpression("article.date", parseDate("21.03.2020"), parseDate("31.03.2020")));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testMustNotInExpression() {
        final Expression expression = new MustNotExpression(new InExpression("article.page", 1, 2, 3));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(1, result.getTotalHitCount());
        assertEquals(3, result.getSearchResultItems().get(0).getId());
    }

    @Test
    public void testMustNotIsNullExpression() {
        final Expression expression = new MustNotExpression(new IsNullExpression("article.rubric"));
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("article.id")).build();
        final SearchResult result = elasticsearchService.search(indexAlias, expression, searchParameter);
        assertEquals(2, result.getTotalHitCount());
        assertEquals(1, result.getSearchResultItems().get(0).getId());
        assertEquals(2, result.getSearchResultItems().get(1).getId());
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Parsing date '" + date + "' failed!", e);
        }
    }

    @Configuration
    @ComponentScan
    protected static class Config {

        @Bean
        IndexPresetConfiguration indexPresetConfiguration() {
            final String indexAlias = "test_index";
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;
            return new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
        }

        @Bean
        List<FieldConfiguration> fieldConfiguration() {
            return Arrays.asList(
                    FieldConfiguration.ID_FIELD,
                    FieldConfiguration.FULLTEXT_FIELD,
                    StandardFieldConfiguration.builder("article", ElasticsearchType.NESTED)
                        .nestedFields(
                                StandardFieldConfiguration.builder(FIELD_NAME_ID, ElasticsearchType.LONG).sortable(true).build(),
                                StandardFieldConfiguration.builder("title", ElasticsearchType.TEXT).copyToFulltext(true).sortable(true).build(),
                                StandardFieldConfiguration.builder("rubric", ElasticsearchType.TEXT).sortable(true).build(),
                                StandardFieldConfiguration.builder("author", ElasticsearchType.TEXT).copyToFulltext(true).build(),
                                StandardFieldConfiguration.builder("page", ElasticsearchType.INTEGER).sortable(true).build(),
                                StandardFieldConfiguration.builder("date", ElasticsearchType.DATE).sortable(true).build()
                        ).build()
            );
        }
    }
}
