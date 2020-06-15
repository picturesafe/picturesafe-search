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
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchAdminImpl;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import de.picturesafe.search.spring.configuration.TestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.config.FieldConfiguration.FIELD_NAME_SUGGEST;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AdditionalFieldParametersIT.Config.class, ElasticsearchAdminImpl.class})
public class AdditionalFieldParametersIT {

    @Autowired
    IndexSetup indexSetup;

    @Autowired
    ElasticsearchAdmin elasticsearchAdmin;

    private final String indexAlias = AdditionalFieldParametersIT.class.getSimpleName().toLowerCase(Locale.ROOT);
    private String indexName;

    @Before
    public void setup() {
        indexName = indexSetup.createIndex(indexAlias);
    }

    @After
    public void tearDown() {
        indexSetup.tearDownIndex(indexAlias);
    }

    @Test
    public void test() {
        final Map<String, Object> mappingProps = getObject(elasticsearchAdmin.getMapping(indexName), "properties");

        Map<String, Object> field = getObject(mappingProps, "simple_text");
        assertEquals(2.0, field.get("boost"));

        field = getObject(mappingProps, "advanced_text");
        assertEquals(true, field.get("store"));

        Map<String, Object> fieldProps = getObject(getObject(mappingProps, "simple_multilingual_text"), "properties");
        field = getObject(fieldProps, "de");
        assertEquals(3.0, field.get("boost"));

        fieldProps = getObject(getObject(mappingProps, "advanced_multilingual_text"), "properties");
        field = getObject(fieldProps, "de");
        assertEquals(true, field.get("store"));

        field = getObject(mappingProps, "keyword_field");
        assertEquals("<empty>", field.get("null_value"));

        field = getObject(mappingProps, "boolean_field");
        assertEquals(4.0, field.get("boost"));

        fieldProps = getObject(getObject(mappingProps, "nested_field"), "properties");
        field = getObject(fieldProps, "nested_text");
        assertEquals(false, field.get("index"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getObject(Map<String, Object> doc, String name) {
        return (Map<String, Object>) doc.get(name);
    }

    @Configuration
    static class Config extends TestConfiguration {

        @Override
        protected List<FieldConfiguration> fieldConfigurations() {
            return Arrays.asList(
                    FieldConfiguration.FULLTEXT_FIELD,
                    StandardFieldConfiguration.builder(
                        "simple_text", ElasticsearchType.TEXT).additionalParameter("boost", 2.0).build(),
                    StandardFieldConfiguration.builder(
                        "advanced_text", ElasticsearchType.TEXT).copyToFulltext(true).aggregatable(true).copyToSuggest(true)
                            .additionalParameter("store", true).build(),
                    StandardFieldConfiguration.builder(
                        "simple_multilingual_text", ElasticsearchType.TEXT).multilingual(true).additionalParameter("boost", 3.0).build(),
                    StandardFieldConfiguration.builder(
                        "advanced_multilingual_text", ElasticsearchType.TEXT).multilingual(true).copyToFulltext(true).aggregatable(true)
                            .copyToSuggest(true).additionalParameter("store", true).build(),
                    StandardFieldConfiguration.builder(
                        "keyword_field", ElasticsearchType.KEYWORD).additionalParameter("null_value", "<empty>").build(),
                    StandardFieldConfiguration.builder(
                        "boolean_field", ElasticsearchType.BOOLEAN).additionalParameter("boost", 4.0).build(),
                    StandardFieldConfiguration.builder(
                        "nested_field", ElasticsearchType.NESTED).nestedFields(
                                StandardFieldConfiguration.builder("nested_text", ElasticsearchType.TEXT)
                                        .additionalParameter("index", false).build())
                            .build(),
                    SuggestFieldConfiguration.name(FIELD_NAME_SUGGEST).additionalParameter("max_input_length", 100)
            );
        }
    }
}
