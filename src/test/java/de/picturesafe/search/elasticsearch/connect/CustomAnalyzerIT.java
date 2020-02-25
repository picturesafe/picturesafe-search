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
import de.picturesafe.search.elasticsearch.config.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@ContextConfiguration(classes = {CustomAnalyzerIT.Config.class}, loader = AnnotationConfigContextLoader.class)
public class CustomAnalyzerIT extends AbstractElasticIntegrationTest {

    private static final String CUSTOM_ANALYZER_NAME = "file_name";

    @Autowired
    IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    MappingConfiguration mappingConfiguration;

    @Autowired
    Elasticsearch elasticsearch;

    @Autowired
    ElasticsearchAdmin elasticsearchAdmin;

    @Test
    public void testCustomAnalyzer() {
        assertNotNull(indexPresetConfiguration.getCustomAnalyzers());
        assertEquals(1, indexPresetConfiguration.getCustomAnalyzers().size());
        assertEquals(CUSTOM_ANALYZER_NAME, indexPresetConfiguration.getCustomAnalyzers().get(0).name());
        assertEquals(2, mappingConfiguration.getFieldConfigurations().size());
        assertNotNull(mappingConfiguration.getFieldConfiguration("filenameWithAnalyzer"));
        assertNotNull(mappingConfiguration.getFieldConfiguration("filenameWithoutAnalyzer"));
        assertEquals(CUSTOM_ANALYZER_NAME, mappingConfiguration.getFieldConfiguration("filenameWithAnalyzer").getAnalyzer());
        assertNull(mappingConfiguration.getFieldConfiguration("filenameWithoutAnalyzer").getAnalyzer());

        final String indexAlias = indexPresetConfiguration.getIndexAlias();
        try {
            elasticsearchAdmin.createIndexWithAlias(indexPresetConfiguration, mappingConfiguration);
            elasticsearch.addToIndex(createTestDocument(1, "test1.pdf"), mappingConfiguration, indexAlias, true);
            elasticsearch.addToIndex(createTestDocument(2, "test2.pdf"), mappingConfiguration, indexAlias, true);
            elasticsearch.addToIndex(createTestDocument(3, "test3.jpg"), mappingConfiguration, indexAlias, true);
            elasticsearch.addToIndex(createTestDocument(4, "test.jpg"), mappingConfiguration, indexAlias, true);
            elasticsearch.addToIndex(createTestDocument(5, "test.jpg"), mappingConfiguration, indexAlias, true);
            elasticsearch.addToIndex(createTestDocument(6, "my_document.doc"), mappingConfiguration, indexAlias, true);

            ElasticsearchResult result = search("filenameWithAnalyzer", "test1.pdf");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "test1");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "pdf");
            assertEquals(2, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "jpg");
            assertEquals(3, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "test");
            assertEquals(2, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "my_document.doc");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "my");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithAnalyzer", "document");
            assertEquals(1, result.getTotalHitCount());

            result = search("filenameWithoutAnalyzer", "test1.pdf");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "test1");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "pdf");
            assertEquals(2, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "jpg");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "test");
            assertEquals(0, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "my_document.doc");
            assertEquals(1, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "my");
            assertEquals(0, result.getTotalHitCount());
            result = search("filenameWithoutAnalyzer", "document");
            assertEquals(0, result.getTotalHitCount());

        } finally {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        }
    }

    @ComponentScan(basePackageClasses = {MultilinguaCustomResolverlIT.class})
    static class Config {

        @Bean
        StandardIndexPresetConfiguration indexPresetConfiguration() {

            final String indexAlias = "local-admin-test-index";
            final int numberOfShards = 1;
            final int numberOfReplicas = 0;

            final StandardIndexPresetConfiguration cfg = new StandardIndexPresetConfiguration(indexAlias, numberOfShards, numberOfReplicas);
            try {
                final IndexSettingsObject fileNameTokenizer = new IndexSettingsObject("file_name_tokenizer");
                fileNameTokenizer.content().startObject()
                        .field("type", "char_group")
                        .array("tokenize_on_chars", "whitespace", ".", "-", "_", "\n")
                        .endObject();
                final IndexSettingsObject fileNameAnalyzer = new IndexSettingsObject(CUSTOM_ANALYZER_NAME);
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

        @Bean
        MappingConfiguration mappingConfiguration() {
            return new MappingConfiguration(testFields());
        }

        private List<FieldConfiguration> testFields() {
            final List<FieldConfiguration> testFields = new ArrayList<>();
            testFields.add(StandardFieldConfiguration.builder("filenameWithAnalyzer", ElasticsearchType.TEXT).analyzer(CUSTOM_ANALYZER_NAME).build());
            testFields.add(StandardFieldConfiguration.builder("filenameWithoutAnalyzer", ElasticsearchType.TEXT).build());
            return testFields;
        }
    }

    private QueryRangeDto defaultRange() {
        return new QueryRangeDto(0, 40);
    }

    private Map<String, Object> createTestDocument(long id, String filename) {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        doc.put("filenameWithAnalyzer", filename);
        doc.put("filenameWithoutAnalyzer", filename);
        return doc;
    }

    private ElasticsearchResult search(String fieldname, String value) {
        final Expression expression = new ValueExpression(fieldname, value);
        final QueryDto queryDto = new QueryDto(expression, defaultRange(), null, null, Locale.GERMAN);
        return elasticsearch.search(queryDto, mappingConfiguration, indexPresetConfiguration);
    }
}

