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

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.LanguageSortConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchHitDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SortOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class MultilinguaCustomResolverlIT extends AbstractElasticIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultilinguaCustomResolverlIT.class);

    @Autowired
    private IndexSetup indexSetup;

    @Autowired
    private MappingConfiguration mappingConfiguration;

    @Autowired
    private Elasticsearch elasticsearch;

    @Autowired
    private ElasticsearchAdmin elasticsearchAdmin;

    @Before
    public void begin() {

        indexSetup.createIndex(elasticsearchAdmin, indexAlias);

        for (int i = 1; i <= 5; i++) {
            final DocumentBuilder documentBuilder = DocumentBuilder.id(i);
            for (LanguageSortConfiguration languageSortConfiguration : mappingConfiguration.getLanguageSortConfigurations()) {
                documentBuilder.put("title." + languageSortConfiguration.getLocale().getLanguage(),
                        "Multilang Titel" + i + " " + languageSortConfiguration.getLocale().getLanguage());
            }
            elasticsearch.addToIndex(documentBuilder.build(), mappingConfiguration, indexAlias, true);
        }
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testSearch() {
        SearchResultDto result = search("Titel3", Locale.GERMANY);
        assertEquals(1, result.getTotalHitCount());
        final SearchHitDto hit = result.getHits().get(0);
        assertEquals("3", hit.getId());
        assertEquals("Multilang Titel3 de", hit.get("title.de"));
        assertEquals("Multilang Titel3 en", hit.get("title.en"));

        result = search("de", Locale.GERMANY);
        assertEquals(5, result.getTotalHitCount());

        result = search("de", Locale.UK);
        assertEquals(0, result.getTotalHitCount());
    }

    @Test
    public void testSort() {
        final SortOption sortOption = SortOption.desc("title");
        final SearchResultDto result = search("Multilang", Locale.GERMANY, Collections.singletonList(sortOption));
        assertEquals(5, result.getTotalHitCount());
        final SearchHitDto hit = result.getHits().get(0);
        assertEquals("5", hit.getId());
        assertEquals("Multilang Titel5 de", hit.get("title.de"));
        assertEquals("Multilang Titel5 en", hit.get("title.en"));
    }

    private SearchResultDto search(String term, Locale locale) {
        return search(term, locale, null);
    }

    private SearchResultDto search(String term, Locale locale, List<SortOption> sortOptions) {
        final Expression expression = new ValueExpression("title", term);
        final QueryRangeDto queryRangeDto = new QueryRangeDto(0, 10);
        final QueryDto queryDto = new QueryDto(expression, queryRangeDto, sortOptions, null, locale);

        final SearchResultDto searchResult = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        LOGGER.debug("{}", searchResult);
        return searchResult;
    }
}
