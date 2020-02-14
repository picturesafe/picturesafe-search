/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFilterDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SortOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class UmlautIT extends AbstractElasticIntegrationTest {

    private static final String[] KEYWORDS = {"bälle", "Bälle", "baelle", "Baelle", "hüpfen", "huepfen", "Kreisssaal",
            "kreißsaal", "Äpfel", "Aepfel", "aepfel", "äpfel"};

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Before
    public void begin() throws Exception {
        indexSetup.setupIndex(indexAlias);

        final Map<String, Object> document = new HashMap<>();
        document.put("id", 42);
        document.put("caption", "Die Bälle hüpfen im Kreißsaal wild herum, Äpfel liegen überall!");
        elasticsearch.addToIndex(document, mappingConfiguration, indexAlias, true);
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void searchUmlautsInField() {
        for (String keyword : KEYWORDS) {
            final Expression expression = new ValueExpression("caption", keyword);
            final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
            final List<QueryFilterDto> queryFilterDtos = new ArrayList<>();
            final List<SortOption> sortOptionList = new ArrayList<>();
            final QueryDto queryDto = new QueryDto(expression, queryRangeDto, queryFilterDtos, sortOptionList, null, Locale.GERMAN);

            final ElasticsearchResult result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

            assertEquals("searching for \"" + keyword + "\" ", 1L, result.getTotalHitCount());
        }
    }

    @Test
    public void searchUmlautsInFulltext() {

        for (String keyword : KEYWORDS) {
            final Expression expression = new FulltextExpression(keyword);
            final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
            final List<QueryFilterDto> queryFilterDtos = new ArrayList<>();
            final List<SortOption> sortOptionList = new ArrayList<>();
            final QueryDto queryDto = new QueryDto(expression, queryRangeDto, queryFilterDtos, sortOptionList, null, Locale.GERMAN);

            final ElasticsearchResult result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

            assertEquals("searching for \"" + keyword + "\" returns " + result.getTotalHitCount() + " instead of 1 result",
                    1L, result.getTotalHitCount());
        }
    }
}
