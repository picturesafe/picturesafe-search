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

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.SortOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.picturesafe.search.parameter.SortOption.ArrayMode.AVG;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MAX;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MEDIAN;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MIN;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.SUM;
import static org.junit.Assert.assertEquals;

public class SortIT extends AbstractElasticIntegrationTest {

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Before
    public void setup() {
        indexSetup.createIndex(indexAlias);

        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.id(1).put("caption", "sorting").put("numbers", Arrays.asList(1, 2, 3, 4)).build(), // sum=10, avg=3
                DocumentBuilder.id(2).put("caption", "sorting").put("numbers", Arrays.asList(1, 2, 3)).build(), // sum=6, avg=2
                DocumentBuilder.id(3).put("caption", "sorting").put("numbers", Arrays.asList(2, 3, 4)).build(), // sum=9, avg=3
                DocumentBuilder.id(4).put("caption", "sorting").put("numbers", Arrays.asList(2, 3)).build()); // sum=5, avg=3
        elasticsearch.addToIndex(indexAlias, true, true, docs);
    }

    @After
    public void tearDown() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testArrayMode() {
        SearchResultDto result = search("sorting", Arrays.asList(SortOption.asc("numbers"), SortOption.desc("id")));
        assertEquals(2, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers"), SortOption.desc("id")));
        assertEquals(3, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.asc("numbers").arrayMode(MIN), SortOption.desc("id")));
        assertEquals(2, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers").arrayMode(MIN), SortOption.desc("id")));
        assertEquals(4, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.asc("numbers").arrayMode(MAX), SortOption.desc("id")));
        assertEquals(4, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers").arrayMode(MAX), SortOption.desc("id")));
        assertEquals(3, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.asc("numbers").arrayMode(SUM), SortOption.desc("id")));
        assertEquals(4, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers").arrayMode(SUM), SortOption.desc("id")));
        assertEquals(1, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.asc("numbers").arrayMode(AVG), SortOption.desc("id")));
        assertEquals(2, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers").arrayMode(AVG), SortOption.desc("id")));
        assertEquals(4, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.asc("numbers").arrayMode(MEDIAN), SortOption.desc("id")));
        assertEquals(2, firstId(result));

        result = search("sorting", Arrays.asList(SortOption.desc("numbers").arrayMode(MEDIAN), SortOption.desc("id")));
        assertEquals(4, firstId(result));
    }

    private SearchResultDto search(String query, List<SortOption> sortOptions) {
        final Expression expression = new FulltextExpression(query);
        final QueryDto queryDto = new QueryDto(expression, defaultRange(), sortOptions, null, Locale.GERMAN);
        return elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
    }

    private QueryRangeDto defaultRange() {
        return new QueryRangeDto(0, 40);
    }

    private int firstId(SearchResultDto result) {
        return Integer.parseInt(result.getHits().get(0).getId());
    }
}
