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
import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.FacetEntryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFilterDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FacetIT  extends AbstractElasticIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacetIT.class);

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Autowired
    @Qualifier("elasticsearchTimeZone")
    String elasticTimeZone;

    @Before
    public void begin() throws Exception {
        indexSetup.setupIndex(indexAlias);
    }

    @After
    public void end() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void testTextFacet() {
        Expression expression = new FulltextExpression("wert");
        QueryFacetDto queryFacetDto = new QueryFacetDto("caption", 10, 100);
        List<QueryFacetDto> queryFacetDtos = Collections.singletonList(queryFacetDto);
        QueryDto queryDto = new QueryDto(expression, defaultRange(), null, null, queryFacetDtos, Locale.GERMAN);
        ElasticsearchResult result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals(1, result.getFacetDtoList().size());
        assertEquals("Facet not working: indexAlias = " + indexAlias, 4, result.getFacetDtoList().get(0).getFacetEntryDtos().size());
        expression = new FulltextExpression("released");
        queryFacetDto = new QueryFacetDto("released", 10, 100);
        queryFacetDtos = Collections.singletonList(queryFacetDto);
        queryDto = new QueryDto(expression, defaultRange(), null, null, queryFacetDtos, Locale.GERMAN);
        result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        assertEquals(1, result.getFacetDtoList().size());
        assertEquals("Facet not working: indexAlias = " + indexAlias, 2, result.getFacetDtoList().get(0).getFacetEntryDtos().size());
    }

    @Test
    public void testFacetResolverOnTextFacet() {
        final Expression expression = new FulltextExpression("wert");
        final QueryFacetDto queryFacetDto = new QueryFacetDto("facetResolved", 10, 100);
        final List<QueryFacetDto> queryFacetDtos = new ArrayList<>();
        queryFacetDtos.add(queryFacetDto);
        final QueryFilterDto queryFilterDto = new QueryFilterDto("caption", "caption1");
        final List<QueryFilterDto> queryFilterDtos = new ArrayList<>();
        queryFilterDtos.add(queryFilterDto);
        final QueryDto queryDto = new QueryDto(expression, defaultRange(), queryFilterDtos, null, queryFacetDtos,
                Locale.GERMAN);
        final ElasticsearchResult result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);

        FacetDto facetDto = null;
        for (FacetDto current : result.getFacetDtoList()) {
            if (current.getName().equals("facetResolved")) {
                facetDto = current;
            }
        }
        assertNotNull("Facet not working: indexAlias = " + indexAlias, facetDto);

        FacetEntryDto facetEntryDto = null;
        for (FacetEntryDto current : facetDto.getFacetEntryDtos()) {
            if (current.getValue().equals("true")) {
                facetEntryDto = current;
            }
        }
        assertNotNull("Resolved facet value not found: indexAlias = " + indexAlias, facetEntryDto);
    }

    @Test
    public void testDateFacet() throws Exception {
        int id = 100;
        elasticsearch.addToIndex(dateDoc(id++, "01.01.2018 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "10.01.2018 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "20.01.2018 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "01.02.2018 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "10.02.2018 12:00:00"), mappingConfiguration, indexAlias, true);

        elasticsearch.addToIndex(dateDoc(id++, "01.01.2017 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "20.01.2017 12:00:00"), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, "01.02.2017 12:00:00"), mappingConfiguration, indexAlias, true);

        // today
        Date date = todayNoon().getTime();
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);
        // yesterday
        date = DateUtils.addDays(date, -1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);

        // last week
        Calendar cal = todayNoon();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        final Date startOfWeek = cal.getTime();
        date = DateUtils.addWeeks(startOfWeek, -1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);
        // last week + 1 day
        date = DateUtils.addDays(date, 1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);
        // last week + 2 days
        date = DateUtils.addDays(date, 1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);

        // last month
        cal = todayNoon();
        cal.set(Calendar.DAY_OF_MONTH, 15);
        final Date middleOfMonth = cal.getTime();
        date = DateUtils.addMonths(middleOfMonth, -1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);
        // last month + 1 day
        date = DateUtils.addDays(date, 1);
        elasticsearch.addToIndex(dateDoc(id++, date), mappingConfiguration, indexAlias, true);

        final Expression expression = new FulltextExpression("DateFacetTest");
        final QueryFacetDto queryFacetDto = new QueryFacetDto("facetDate", 10, 100);
        final QueryDto queryDto = new QueryDto(expression, defaultRange(), null, null, Collections.singletonList(queryFacetDto),
                Locale.GERMAN);
        final ElasticsearchResult result = elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
        LOGGER.debug("Search result for time zone elastic='{}', system='{}':\n{}", elasticTimeZone, TimeZone.getDefault().getID(), result);
        assertEquals(id -100, result.getTotalHitCount());

        final List<FacetDto> facetDtos = result.getFacetDtoList();
        assertEquals(2, facetDtos.size());

        final FacetDto rangesDto = facetDtos.get(0);
        LOGGER.debug("Ranges facet:\n{}", rangesDto);
        assertEquals("ranges", rangesDto.getName());
        // Total count = (1 today + 2 yesterday) * 2 [in this or last week] + 3 last week  + 2 last month
        // + (1 today + 2 yesterday + 3 last week) [in this or last month] = 17
        assertEquals(17, rangesDto.getCount());
        assertEquals(6, rangesDto.getFacetEntryDtos().size());
        final Map<String, FacetEntryDto> rangeEntries = rangesDto.getFacetEntryDtos().stream().collect(Collectors.toMap(f -> f.getValue().toString(), f -> f));
        final FacetEntryDto today = rangeEntries.get("today");
        assertEquals("Today should have 1 entry", 1, today.getCount());
        final FacetEntryDto yesterday = rangeEntries.get("yesterday");
        assertTrue("Yesterday should have at least 2 entries", yesterday.getCount() >= 2);
        final FacetEntryDto week = rangeEntries.get("week");
        assertTrue("Week should have at least 1 entry", week.getCount() >= 1);
        final FacetEntryDto lastWeek = rangeEntries.get("last week");
        assertTrue("Last week should have at least 3 entries", lastWeek.getCount() >= 3);
        final FacetEntryDto month = rangeEntries.get("month");
        assertTrue("Month should have at least 1 entry", month.getCount() >= 1);
        final FacetEntryDto lastMonth = rangeEntries.get("last month");
        assertTrue("Last month should have at least 2 entries", lastMonth.getCount() >= 2);

        final FacetDto yearsDto = facetDtos.get(1);
        LOGGER.debug("Years facet:\n{}", yearsDto);
        assertEquals(16, yearsDto.getCount());
        assertTrue("Years count should be 3 for most of the year or 4 when in january",
                yearsDto.getFacetEntryDtos().size() == 3 || yearsDto.getFacetEntryDtos().size() == 4);
        final Map<String, FacetEntryDto> yearEntries = yearsDto.getFacetEntryDtos().stream().collect(Collectors.toMap(f -> f.getValue().toString(), f -> f));
        final String thisYearNumber = DateFormatUtils.format(new Date(), "yyyy");
        final FacetEntryDto thisYear = yearEntries.get(thisYearNumber);
        assertTrue("This year should have at least 1 entry", thisYear.getCount() >= 1);
        final FacetEntryDto year2018 = yearEntries.get("2018");
        assertEquals("Year 2018 should have 5 entries", 5, year2018.getCount());
        final FacetEntryDto year2017 = yearEntries.get("2017");
        assertEquals("Year 2017 should have 3 entries", 3, year2017.getCount());
    }

    private Calendar todayNoon() {
        final Calendar cal = Calendar.getInstance(Locale.GERMAN);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private Map<String, Object> dateDoc(long id, String date) throws Exception {
        return dateDoc(id, DateUtils.parseDate(date, "dd.MM.yyyy HH:mm:ss"));
    }

    private Map<String, Object> dateDoc(long id, Date date) {
        final String caption = "DateFacetTest #" + id + " - " + DateFormatUtils.format(date, "dd.MM.yyyy");
        LOGGER.debug(caption);
        final Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("caption", caption);
        doc.put("facetDate", date);
        return doc;
    }

    private QueryRangeDto defaultRange() {
        return new QueryRangeDto(0, 40);
    }
}
