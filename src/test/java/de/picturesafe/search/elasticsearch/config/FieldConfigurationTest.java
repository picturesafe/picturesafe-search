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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

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
        FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT).build();

        assertEquals("textField", fieldConfiguration.getName());
        assertEquals(ElasticsearchType.TEXT.toString(), fieldConfiguration.getElasticsearchType());
        assertFalse(fieldConfiguration.isCopyToFulltext());
        assertFalse(fieldConfiguration.isAggregatable());
        assertFalse(fieldConfiguration.isSortable());
        assertFalse(fieldConfiguration.isMultilingual());
        assertFalse(fieldConfiguration.isNestedObject());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT)
                .copyToFulltext(true)
                .aggregatable(true)
                .sortable(true)
                .multilingual(true)
                .analyzer("specialAnalyzer")
                .build();

        assertTrue(fieldConfiguration.isCopyToFulltext());
        assertTrue(fieldConfiguration.isAggregatable());
        assertTrue(fieldConfiguration.isSortable());
        assertTrue(fieldConfiguration.isMultilingual());
        assertEquals("specialAnalyzer", fieldConfiguration.getAnalyzer());
    }

    @Test
    public void testInvalidFieldNames() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Parameter 'name' must not contain a '.'!");
        StandardFieldConfiguration.builder("text.field", ElasticsearchType.TEXT).build();

        new SuggestFieldConfiguration("suggest.field");
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Parameter 'name' must not contain a '.'!");
    }

    @Test
    public void testNestedFieldFieldConfiguration() {
        final List<StandardFieldConfiguration> nestedFields = new ArrayList<>();
        nestedFields.add(StandardFieldConfiguration.builder("nestedTestField", ElasticsearchType.TEXT).build());
        nestedFields.add(StandardFieldConfiguration.builder("nestedIntegerField", ElasticsearchType.INTEGER).build());
        nestedFields.add(StandardFieldConfiguration.builder("nestedDateField", ElasticsearchType.DATE).build());

        final FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("nestedField", ElasticsearchType.NESTED)
                .nestedFields(nestedFields)
                .build();

        assertTrue(fieldConfiguration.isNestedObject());
        assertNotNull(fieldConfiguration.getNestedFields());
        assertEquals(3, fieldConfiguration.getNestedFields().size());
        assertNotNull(fieldConfiguration.getNestedField("nestedTestField"));
        assertEquals("nestedTestField", fieldConfiguration.getNestedField("nestedTestField").getName());
        assertEquals(ElasticsearchType.TEXT.toString(), fieldConfiguration.getNestedField("nestedTestField").getElasticsearchType());
    }

    @Test
    public void testSuggestFieldConfiguration() {
        FieldConfiguration fieldConfiguration = new SuggestFieldConfiguration("mySuggestField");
        assertEquals("mySuggestField", fieldConfiguration.getName());
        assertEquals(ElasticsearchType.COMPLETION.toString(), fieldConfiguration.getElasticsearchType());
        assertFalse(fieldConfiguration.isCopyToFulltext());
        assertFalse(fieldConfiguration.isAggregatable());
        assertFalse(fieldConfiguration.isSortable());
        assertFalse(fieldConfiguration.isMultilingual());
        assertFalse(fieldConfiguration.isNestedObject());

        fieldConfiguration = FieldConfiguration.SUGGEST_FIELD;
        assertEquals(FieldConfiguration.FIELD_NAME_SUGGEST, fieldConfiguration.getName());
        assertEquals(ElasticsearchType.COMPLETION.toString(), fieldConfiguration.getElasticsearchType());
    }

    @Test
    public void testCopyTo() {
        FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT).copyTo("1", "2", "3").build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT).copyTo("1").copyTo("2").copyTo("3").build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT).copyTo(Arrays.asList("1", "2", "3")).build();
        assertEquals(new TreeSet<>(Arrays.asList("1", "2", "3")), fieldConfiguration.getCopyToFields());

        fieldConfiguration = StandardFieldConfiguration.builder("textField", ElasticsearchType.TEXT).copyTo("1", "2", "3")
                .copyTo((Collection<String>) null).build();
        assertNull(fieldConfiguration.getCopyToFields());
    }
}
