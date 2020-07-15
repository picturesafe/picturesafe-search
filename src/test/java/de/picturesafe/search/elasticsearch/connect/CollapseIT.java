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
import de.picturesafe.search.elasticsearch.connect.dto.SearchHitDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.CollapseOption;
import de.picturesafe.search.parameter.InnerHitsOption;
import de.picturesafe.search.parameter.SortOption;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CollapseIT extends AbstractElasticIntegrationTest {

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    final Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
    final Date tomorrow = DateUtils.addDays(today, 1);
    final Date yesterday = DateUtils.addDays(today, -1);

    @Before
    public void setup() {
        indexSetup.createIndex(indexAlias);

        final List<Map<String, Object>> docs = Arrays.asList(
                DocumentBuilder.id(10).put("caption", "collapse")
                        .put("keywordField", "a").put("numbers", 2).put("createDate", today).build(),
                DocumentBuilder.id(11).put("caption", "collapse")
                        .put("keywordField", "a").put("numbers", 2).put("createDate", tomorrow).build(),
                DocumentBuilder.id(12).put("caption", "collapse")
                        .put("keywordField", "a").put("numbers", 2).put("createDate", yesterday).build(),
                DocumentBuilder.id(20).put("caption", "collapse")
                        .put("keywordField", "a").put("numbers", 1).put("createDate", today).build(),
                DocumentBuilder.id(21).put("caption", "collapse")
                        .put("keywordField", "a").put("numbers", 1).put("createDate", yesterday).build(),
                DocumentBuilder.id(30).put("caption", "collapse")
                        .put("keywordField", "b").put("numbers", 11).put("createDate", today).build(),
                DocumentBuilder.id(31).put("caption", "collapse")
                        .put("keywordField", "b").put("numbers", 11).put("createDate", tomorrow).build(),
                DocumentBuilder.id(32).put("caption", "collapse")
                        .put("keywordField", "b").put("numbers", 11).put("createDate", yesterday).build(),
                DocumentBuilder.id(40).put("caption", "collapse")
                        .put("keywordField", "b").put("numbers", 12).put("createDate", today).build(),
                DocumentBuilder.id(41).put("caption", "collapse")
                        .put("keywordField", "b").put("numbers", 12).put("createDate", yesterday).build(),
                DocumentBuilder.id(50).put("caption", "collapse")
                        .put("keywordField", "c").put("numbers", 21).put("createDate", yesterday).build());
        elasticsearch.addToIndex(indexAlias, true, true, docs);
    }

    @After
    public void tearDown() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testCollapse() {
        final SearchResultDto result = search("collapse", CollapseOption.field("keywordField"),
                SortOption.asc("keywordField"), SortOption.desc("numbers"));
        assertEquals(3, result.getHits().size());

        SearchHitDto hit = result.getHits().get(0);
        assertEquals("a", hit.get("keywordField"));
        assertEquals(2, hit.get("numbers"));

        hit = result.getHits().get(1);
        assertEquals("b", hit.get("keywordField"));
        assertEquals(12, hit.get("numbers"));

        hit = result.getHits().get(2);
        assertEquals("c", hit.get("keywordField"));
        assertEquals(21, hit.get("numbers"));
    }

    @Test
    public void testCollapseWithInnerHits() {
        final SearchResultDto result = search("collapse", CollapseOption.field("keywordField")
                        .innerHits(InnerHitsOption.name("newest").size(2).sortOptions(SortOption.desc("numbers"), SortOption.desc("createDate"))),
                SortOption.asc("keywordField"), SortOption.desc("numbers"), SortOption.desc("createDate"));
        assertEquals(3, result.getHits().size());

        SearchHitDto hit = result.getHits().get(0);
        assertEquals("a", hit.get("keywordField"));
        assertEquals(2, hit.get("numbers"));
        assertEquals(tomorrow, ElasticDateUtils.parseIso((String) hit.get("createDate")));
        List<SearchHitDto> innerHits = hit.getInnerHits().get("newest");
        assertEquals(2, innerHits.size());
        SearchHitDto innerHit = innerHits.get(0);
        assertEquals(tomorrow, ElasticDateUtils.parseIso((String) innerHit.get("createDate")));
        innerHit = innerHits.get(1);
        assertEquals(today, ElasticDateUtils.parseIso((String) innerHit.get("createDate")));

        hit = result.getHits().get(1);
        assertEquals("b", hit.get("keywordField"));
        assertEquals(12, hit.get("numbers"));
        assertEquals(today, ElasticDateUtils.parseIso((String) hit.get("createDate")));
        innerHits = hit.getInnerHits().get("newest");
        assertEquals(2, innerHits.size());
        innerHit = innerHits.get(0);
        assertEquals(today, ElasticDateUtils.parseIso((String) innerHit.get("createDate")));
        innerHit = innerHits.get(1);
        assertEquals(yesterday, ElasticDateUtils.parseIso((String) innerHit.get("createDate")));

        hit = result.getHits().get(2);
        assertEquals("c", hit.get("keywordField"));
        assertEquals(21, hit.get("numbers"));
        assertEquals(yesterday, ElasticDateUtils.parseIso((String) hit.get("createDate")));
        innerHits = hit.getInnerHits().get("newest");
        assertEquals(1, innerHits.size());
        innerHit = innerHits.get(0);
        assertEquals(yesterday, ElasticDateUtils.parseIso((String) innerHit.get("createDate")));
    }

    private SearchResultDto search(String query, CollapseOption collapseOption, SortOption... sortOptions) {
        final Expression expression = new FulltextExpression(query);
        final QueryDto queryDto = new QueryDto(expression, Locale.GERMAN).queryRange(defaultRange()).collapseOption(collapseOption).sortOptions(sortOptions);
        return elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
    }

    private QueryRangeDto defaultRange() {
        return new QueryRangeDto(0, 40);
    }
}
