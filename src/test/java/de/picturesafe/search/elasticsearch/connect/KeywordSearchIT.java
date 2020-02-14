/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFilterDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.ConditionExpression;
import de.picturesafe.search.expression.KeywordExpression;
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

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_EQ;
import static org.junit.Assert.assertEquals;

public class KeywordSearchIT extends AbstractElasticIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordSearchIT.class);

    private static final String[] KEYWORDS = {"dies", "das", "dies und das"};

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Before
    public void begin() {
        indexSetup.createIndex(indexAlias);

        final Map<String, Object> document = new HashMap<>();
        int i = 1;
        for (String keyword : KEYWORDS) {
            document.put("id", i++);
            document.put("keyword", keyword);
            elasticsearch.addToIndex(document, mappingConfiguration, indexAlias, true);
        }
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testEquals() {
        for (String keyword : KEYWORDS) {
            final ElasticsearchResult result = keywordSearch(keyword);
            assertEquals("searching for \"" + keyword + "\" ", 1L, result.getTotalHitCount());
        }

        assertEquals(0, keywordSearch("dies und").getTotalHitCount());
        assertEquals(0, keywordSearch("und das").getTotalHitCount());
        assertEquals(0, keywordSearch("dies das").getTotalHitCount());
    }

    @Test
    public void testNotEquals() {
        assertEquals(2, keywordSearch("dies und das", NOT_EQ).getTotalHitCount());
        assertEquals(2, keywordSearch("dies", NOT_EQ).getTotalHitCount());
        assertEquals(3, keywordSearch("und", NOT_EQ).getTotalHitCount());
    }

    private ElasticsearchResult keywordSearch(String term) {
        return keywordSearch(term, EQ);
    }

    private ElasticsearchResult keywordSearch(String term, ConditionExpression.Comparison comparison) {
        final KeywordExpression expression = new KeywordExpression("keyword", comparison, term);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
        final List<QueryFilterDto> queryFilterDtos = new ArrayList<>();
        final List<SortOption> sortOptionList = new ArrayList<>();
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, queryFilterDtos, sortOptionList,
                                               null, Locale.GERMAN);

        final ElasticsearchResult searchResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        LOGGER.debug("{}", searchResult);
        return searchResult;
    }
}
