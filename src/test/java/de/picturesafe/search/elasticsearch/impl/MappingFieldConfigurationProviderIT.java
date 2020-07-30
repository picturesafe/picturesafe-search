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

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.IndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.spring.configuration.DefaultElasticConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.config.FieldConfiguration.FIELD_NAME_ID;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DefaultElasticConfiguration.class, MappingFieldConfigurationProviderIT.Config.class, ElasticsearchServiceImpl.class},
        loader = AnnotationConfigContextLoader.class)
public class MappingFieldConfigurationProviderIT {

    @Autowired
    private IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    private List<FieldConfiguration> fieldConfigurations;

    @Autowired
    private List<Locale> indexLocales;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchAdmin elasticsearchAdmin;

    private MappingFieldConfigurationProvider mappingFieldConfigurationProvider;

    @Before
    public void setup() {
        mappingFieldConfigurationProvider = new MappingFieldConfigurationProvider(elasticsearchAdmin);
        elasticsearchService.createIndexWithAlias(indexPresetConfiguration.getIndexAlias());
    }

    @After
    public void teardown() {
        elasticsearchService.deleteIndexWithAlias(indexPresetConfiguration.getIndexAlias());
    }

    @Test
    public void testGetFieldConfigurations() {
        assertEquals(sortedFields(fieldConfigurations),
                sortedFields(mappingFieldConfigurationProvider.getFieldConfigurations(indexPresetConfiguration.getIndexAlias())));
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(sortedLocales(indexLocales),
                sortedLocales(mappingFieldConfigurationProvider.getSupportedLocales(indexPresetConfiguration.getIndexAlias())));
    }

    private List<? extends FieldConfiguration> sortedFields(List<? extends FieldConfiguration> fieldConfigurations) {
        fieldConfigurations.sort(Comparator.comparing(FieldConfiguration::getName));
        fieldConfigurations.forEach(field -> {
            if (field.isNestedObject()) {
                field.getNestedFields().sort(Comparator.comparing(FieldConfiguration::getName));
            }
        });
        return  fieldConfigurations;
    }

    private List<Locale> sortedLocales(List<Locale> locales) {
        locales.sort(Comparator.comparing(Locale::toString));
        return locales;
    }

    @Configuration
    @ComponentScan(basePackages = "de.picturesafe.search.elasticsearch.connect")
    protected static class Config {

        @Bean
        IndexPresetConfiguration indexPresetConfiguration() {
            final String indexAlias = MappingFieldConfigurationProviderIT.class.getSimpleName().toLowerCase(Locale.ROOT);
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;
            return new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
        }

        @Bean
        IndexPresetConfigurationProvider indexPresetConfigurationProvider(IndexPresetConfiguration indexPresetConfiguration) {
            return new StaticIndexPresetConfigurationProvider(indexPresetConfiguration);
        }

        @Bean
        List<FieldConfiguration> fieldConfigurations() {
            return Arrays.asList(
                    FieldConfiguration.FULLTEXT_FIELD,
                    FieldConfiguration.SUGGEST_FIELD,
                    StandardFieldConfiguration.builder("multilang", ElasticsearchType.TEXT).copyToFulltext(true).sortable(true).aggregatable(true)
                            .multilingual(true).build(),
                    StandardFieldConfiguration.builder("keyword", ElasticsearchType.KEYWORD).sortable(true).aggregatable(true).build(),
                    StandardFieldConfiguration.builder("count", ElasticsearchType.INTEGER).sortable(true).aggregatable(true).build(),
                    StandardFieldConfiguration.builder("article", ElasticsearchType.NESTED)
                        .nestedFields(
                                StandardFieldConfiguration.builder(FIELD_NAME_ID, ElasticsearchType.LONG).sortable(true).aggregatable(true).build(),
                                StandardFieldConfiguration.builder("title", ElasticsearchType.TEXT).copyToFulltext(true).sortable(true).aggregatable(true)
                                        .build(),
                                StandardFieldConfiguration.builder("rubric", ElasticsearchType.TEXT).sortable(true).aggregatable(true).build(),
                                StandardFieldConfiguration.builder("author", ElasticsearchType.TEXT).copyToFulltext(true).build(),
                                StandardFieldConfiguration.builder("page", ElasticsearchType.INTEGER).sortable(true).aggregatable(true).build(),
                                StandardFieldConfiguration.builder("date", ElasticsearchType.DATE).sortable(true).aggregatable(true).build()
                        ).build()
            );
        }

        @Bean
        public List<Locale> indexLocales() {
            return Arrays.asList(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH);
        }
    }
}
