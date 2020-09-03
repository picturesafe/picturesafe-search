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

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.SuggestFieldConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.BOOLEAN;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.COMPLETION;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.DATE;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.INTEGER;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.NESTED;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.OBJECT;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.TEXT;
import static de.picturesafe.search.elasticsearch.config.FieldConfiguration.FIELD_NAME_SUGGEST;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

public class FieldConfigurationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testStandardFieldConfiguration() {
        FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).build();

        assertEquals("textField", fieldConfiguration.getName());
        assertEquals(TEXT.getElasticType(), fieldConfiguration.getElasticsearchType());
        assertFalse(fieldConfiguration.isCopyToFulltext());
        assertFalse(fieldConfiguration.isAggregatable());
        assertFalse(fieldConfiguration.isSortable());
        assertFalse(fieldConfiguration.isMultilingual());
        assertFalse(fieldConfiguration.isNestedObject());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT)
                .copyToFulltext(true)
                .aggregatable(true)
                .sortable(true)
                .multilingual(true)
                .analyzer("specialAnalyzer")
                .additionalParameter("abc", "def")
                .build();

        assertTrue(fieldConfiguration.isCopyToFulltext());
        assertTrue(fieldConfiguration.isAggregatable());
        assertTrue(fieldConfiguration.isSortable());
        assertTrue(fieldConfiguration.isMultilingual());
        assertEquals("specialAnalyzer", fieldConfiguration.getAnalyzer());
        assertNotNull(fieldConfiguration.getAdditionalParameters());
        assertEquals("def", fieldConfiguration.getAdditionalParameters().get("abc"));
    }

    @Test
    public void testInvalidFieldNames() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Parameter 'name' must not contain a '.'!");
        StandardFieldConfiguration.builder("text.field", TEXT).build();

        SuggestFieldConfiguration.name("suggest.field");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Parameter 'name' must not contain a '.'!");
    }

    @Test
    public void testNestedFieldFieldConfiguration() {
        final FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("nestedField", NESTED)
                .innerFields(StandardFieldConfiguration.builder("innerTextField", TEXT).build(),
                             StandardFieldConfiguration.builder("innerIntegerField", INTEGER).build(),
                             StandardFieldConfiguration.builder("innerDateField", DATE).build())
                .build();

        assertTrue(fieldConfiguration.isNestedObject());
        assertTrue(fieldConfiguration.hasInnerFields());
        assertNotNull(fieldConfiguration.getInnerFields());
        assertEquals(3, fieldConfiguration.getInnerFields().size());
        assertNotNull(fieldConfiguration.getInnerField("innerTextField"));
        assertEquals("innerTextField", fieldConfiguration.getInnerField("innerTextField").getName());
        assertEquals(TEXT.getElasticType(), fieldConfiguration.getInnerField("innerTextField").getElasticsearchType());
    }

    @Test
    public void testObjectFieldFieldConfiguration() {
        final FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("objectField", OBJECT)
                .innerFields(StandardFieldConfiguration.builder("innerTextField", TEXT).build(),
                             StandardFieldConfiguration.builder("innerIntegerField", INTEGER).build(),
                             StandardFieldConfiguration.builder("innerDateField", DATE).build())
                .build();

        assertTrue(fieldConfiguration.hasInnerFields());
        assertNotNull(fieldConfiguration.getInnerFields());
        assertEquals(3, fieldConfiguration.getInnerFields().size());
        assertNotNull(fieldConfiguration.getInnerField("innerDateField"));
        assertEquals("innerDateField", fieldConfiguration.getInnerField("innerDateField").getName());
        assertEquals(DATE.getElasticType(), fieldConfiguration.getInnerField("innerDateField").getElasticsearchType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInnerFieldsWrongElasticTypeError() {
        StandardFieldConfiguration.builder("test", TEXT)
                .innerFields(StandardFieldConfiguration.builder("inner", BOOLEAN).build()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInnerFieldsWrongClassError() {
        StandardFieldConfiguration.builder("test", OBJECT)
                .innerFields(Collections.singletonList(SuggestFieldConfiguration.name("suggest"))).build();
    }

    @Test
    public void testSuggestFieldConfiguration() {
        FieldConfiguration fieldConfiguration = SuggestFieldConfiguration.name("mySuggestField").additionalParameter("max_input_length", 100);
        assertEquals("mySuggestField", fieldConfiguration.getName());
        assertEquals(COMPLETION.getElasticType(), fieldConfiguration.getElasticsearchType());
        assertFalse(fieldConfiguration.isCopyToFulltext());
        assertFalse(fieldConfiguration.isAggregatable());
        assertFalse(fieldConfiguration.isSortable());
        assertFalse(fieldConfiguration.isMultilingual());
        assertFalse(fieldConfiguration.isNestedObject());
        assertNotNull(fieldConfiguration.getAdditionalParameters());
        assertEquals(100, fieldConfiguration.getAdditionalParameters().get("max_input_length"));

        fieldConfiguration = FieldConfiguration.SUGGEST_FIELD;
        assertEquals(FIELD_NAME_SUGGEST, fieldConfiguration.getName());
        assertEquals(COMPLETION.getElasticType(), fieldConfiguration.getElasticsearchType());
        assertNull(fieldConfiguration.getAdditionalParameters());
    }

    @Test
    public void testCopyTo() {
        FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).copyTo("1", "2", "3").build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).copyTo("1").copyTo("2").copyTo("3").build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).copyTo(Arrays.asList("1", "2", "3")).build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).copyTo((Collection<String>) null).build();
        assertNull(fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", TEXT).copyTo("1", "2", "3")
                .copyTo((Collection<String>) null).build();
        assertNull(fieldConfiguration.getCopyToFields());
    }
}
