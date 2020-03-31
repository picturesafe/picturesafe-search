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

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import com.jayway.jsonpath.JsonPath;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.AbstractTimeZoneRelatedTest;
import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.expression.ConditionExpression;
import de.picturesafe.search.expression.DayExpression;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GT;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DayExpressionFilterBuilderTest extends AbstractTimeZoneRelatedTest {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String offset;

    @Before
    public void setup() throws Exception {
        final Date date = DateUtils.parseDate("2016-04-07 12:00:00", "yyyy-MM-dd HH:mm:ss");
        offset = ElasticDateUtils.getOffset(timeZone, date);
    }

    @Test
    public void equals() throws Exception {
        final String json = jsonFromFilterBuilder(EQ);

        assertEquals("2016-04-07T00:00:00" + offset, JsonPath.read(json, "$.bool.should[0].range.createDate.from"));
        assertEquals("2016-04-08T00:00:00" + offset, JsonPath.read(json, "$.bool.should[0].range.createDate.to"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.bool.should[0].range.createDate.include_lower"));
        assertEquals(Boolean.FALSE, JsonPath.read(json, "$.bool.should[0].range.createDate.include_upper"));
        assertEquals("2016-04-07T00:00:00" + offset, JsonPath.read(json, "$.bool.should[1].term.createDate.value"));
    }

    @Test
    public void greaterThan() throws Exception {
        final String json = jsonFromFilterBuilder(GT);

        assertEquals("2016-04-08T00:00:00" + offset, JsonPath.read(json, "$.range.createDate.from"));
        assertNull(null, JsonPath.read(json, "$.range.createDate.to"));
        assertEquals(Boolean.FALSE, JsonPath.read(json, "$.range.createDate.include_lower"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.range.createDate.include_upper"));
    }

    @Test
    public void lessThan() throws Exception {
        final String json = jsonFromFilterBuilder(LT);

        assertEquals("2016-04-07T00:00:00" + offset, JsonPath.read(json, "$.range.createDate.to"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.range.createDate.include_lower"));
        assertEquals(Boolean.FALSE, JsonPath.read(json, "$.range.createDate.include_upper"));
    }

    @Test
    public void lessEquals() throws Exception {
        final String json = jsonFromFilterBuilder(LE);

        assertNull(JsonPath.read(json, "$.range.createDate.from"));
        assertEquals("2016-04-08T00:00:00" + offset, JsonPath.read(json, "$.range.createDate.to"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.range.createDate.include_lower"));
        assertEquals(Boolean.FALSE, JsonPath.read(json, "$.range.createDate.include_upper"));
    }

    @Test
    public void greaterEquals() throws Exception {
        final String json = jsonFromFilterBuilder(GE);

        assertEquals("2016-04-07T00:00:00" + offset, JsonPath.read(json, "$.range.createDate.from"));
        assertNull(JsonPath.read(json, "$.range.createDate.to"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.range.createDate.include_lower"));
        assertEquals(Boolean.TRUE, JsonPath.read(json, "$.range.createDate.include_upper"));
    }

    private static String jsonFromFilterBuilder(ConditionExpression.Comparison comparison) throws ParseException, IOException {
        final ExpressionFilterBuilderContext context = createExpressionBuilderContext(comparison);
        final DayExpressionFilterBuilder dayExpressionFilterBuilder = new DayExpressionFilterBuilder("Europe/Berlin");
        final QueryBuilder builder = dayExpressionFilterBuilder.buildFilter(context);

        return getJsonString(builder);
    }

    private static String getJsonString(QueryBuilder builder) throws IOException {
        final XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().prettyPrint();
        return Strings.toString(builder.toXContent(jsonBuilder, ToXContent.EMPTY_PARAMS));
    }

    private static ExpressionFilterBuilderContext createExpressionBuilderContext(ConditionExpression.Comparison comparison) throws ParseException {
        final String field = "createDate";
        final String dateString = "2016-04-07 10:10:10";

        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT);
        final Date date = dateFormat.parse(dateString);

        final List<FieldConfiguration> fieldConfigs = new ArrayList<>();
        fieldConfigs.add(StandardFieldConfiguration.builder("createDate", ElasticsearchType.DATE).build());

        final MappingConfiguration indexConfig = new MappingConfiguration(fieldConfigs);
        return new ExpressionFilterBuilderContext(new DayExpression(field, comparison, date), new SearchContext(null, indexConfig), null);
    }
}
