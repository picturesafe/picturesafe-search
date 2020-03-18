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

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexSettingsObject;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.error.IndexCreateException;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = {AdminIT.Config.class}, loader = AnnotationConfigContextLoader.class)
public class AdminIT extends AbstractElasticIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminIT.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    @Qualifier("adminIndexPresetConfiguration")
    IndexPresetConfiguration adminIndexPresetConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Autowired
    ElasticsearchAdmin elasticsearchAdmin;

    @Test
    public void testGetMapping() {
        final String indexAlias = adminIndexPresetConfiguration.getIndexAlias();
        try {
            final String newIndexName = elasticsearchAdmin.createIndexWithAlias(adminIndexPresetConfiguration, mappingConfiguration);
            final String mapping = elasticsearchAdmin.getMappingAsJson(newIndexName);
            LOG.info(mapping);
            assertNotNull("Got no index data for configured index", mapping);
        } finally {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        }
    }

    @Test
    public void testCreateAndDeleteIndex() throws Exception {
        final String indexAlias = adminIndexPresetConfiguration.getIndexAlias();

        assertFalse("Index should not exist", elasticsearchAdmin.aliasOrIndexExists(indexAlias));
        try {
            final String indexName = elasticsearchAdmin.createIndexWithAlias(adminIndexPresetConfiguration, mappingConfiguration);
            assertTrue("Created index should exist", elasticsearchAdmin.aliasOrIndexExists(indexAlias));

            final GetSettingsResponse response = restClient.indices().getSettings(new GetSettingsRequest().indices(indexName), RequestOptions.DEFAULT);
            final Settings settings = response.getIndexToSettings().get(indexName);
            assertEquals("char_group", settings.get("index.analysis.tokenizer.file_name_tokenizer.type"));
            assertEquals("file_name_tokenizer", settings.get("index.analysis.analyzer.file_name.tokenizer"));

            elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            assertFalse("Deleted index must not exist", elasticsearchAdmin.aliasOrIndexExists(indexAlias));
        } finally {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        }
    }

    @Test
    public void testUpdateMapping() throws Exception {
        final String fieldName = "update_test";
        final String indexAlias = adminIndexPresetConfiguration.getIndexAlias();

        try {
            final String indexName = elasticsearchAdmin.createIndexWithAlias(adminIndexPresetConfiguration, mappingConfiguration);
            assertTrue("Created index should exist", elasticsearchAdmin.aliasOrIndexExists(indexAlias));

            final FieldConfiguration fieldConfig = StandardFieldConfiguration.builder(fieldName, ElasticsearchType.TEXT).copyToFulltext(true).build();
            elasticsearchAdmin.updateMapping(adminIndexPresetConfiguration, mappingConfiguration, Collections.singletonList(fieldConfig));

            final GetMappingsResponse response = restClient.indices().getMapping(new GetMappingsRequest().indices(indexName), RequestOptions.DEFAULT);
            final MappingMetaData mapping = response.mappings().get(indexName);
            final Map<String, Object> properties = (Map<String, Object>) mapping.sourceAsMap().get("properties");
            assertTrue("Mapping should contain new field", properties.containsKey(fieldName));

            elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            assertFalse("Deleted index must not exist", elasticsearchAdmin.aliasOrIndexExists(indexAlias));
        } finally {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        }
    }

    @Test
    public void testFieldsLimit() {
        final StandardIndexPresetConfiguration indexPresetConfiguration = ((StandardIndexPresetConfiguration) adminIndexPresetConfiguration).clone();
        final String indexAlias = indexPresetConfiguration.getIndexAlias();

        try {
            indexPresetConfiguration.setFieldsLimit(2);
            exception.expect(IndexCreateException.class);
            elasticsearchAdmin.createIndexWithAlias(indexPresetConfiguration, mappingConfiguration);
        } finally {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        }
    }

    @ComponentScan(basePackageClasses = {MultilinguaCustomResolverlIT.class})
    static class Config {

        @Bean
        StandardIndexPresetConfiguration adminIndexPresetConfiguration() {

            final String indexAlias = "local-admin-test-index";
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;

            final StandardIndexPresetConfiguration cfg = new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
            try {
                IndexSettingsObject fileNameTokenizer = new IndexSettingsObject("file_name_tokenizer");
                fileNameTokenizer.content().startObject()
                        .field("type", "char_group")
                        .array("tokenize_on_chars", "whitespace", ".", "-", "_", "\n")
                        .endObject();
                IndexSettingsObject fileNameAnalyzer = new IndexSettingsObject("file_name");
                fileNameAnalyzer.content().startObject()
                        .field("type", "custom")
                        .field("tokenizer", "file_name_tokenizer")
                        .array("filter", "lowercase")
                        .endObject();
                cfg.setCustomTokenizers(Collections.singletonList(fileNameTokenizer));
                cfg.setCustomAnalyzers(Collections.singletonList(fileNameAnalyzer));
            } catch (IOException e) {
                throw new RuntimeException("Failed to set custom analyzer!", e);
            }
            return cfg;
        }
    }
}

