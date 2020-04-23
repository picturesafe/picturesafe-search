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

import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.IndexObject;

import java.util.List;
import java.util.Map;

/**
 * Presets for index creation
 */
public interface IndexPresetConfiguration extends IndexObject<IndexPresetConfiguration> {

    /**
     * Gets the alias name.
     * @return Alias name
     */
    String getIndexAlias();

    /**
     * Gets the number of shards.
     * @return Number of shards
     */
    int getNumberOfShards();

    /**
     * Gets the number of replicas.
     * @return Number of replicas
     */
    int getNumberOfReplicas();

    /**
     * Gets the maximum result window size (maximum number of hits in a search result).
     * @return Maximum result window size
     */
    int getMaxResultWindow();

    /**
     * Gets the maximum number of fields in the index.
     * @return Maximum number of fields in the index (null = use default)
     */
    Integer getFieldsLimit();

    /**
     * Checks if compression should be used.
     * @return TRUE if compression should be used
     */
    boolean isUseCompression();

    /**
     * Gets optional custom tokenizers.
     * @return Custom tokenizers
     */
    List<IndexSettingsObject> getCustomTokenizers();

    /**
     * Gets optional custom analyzers.
     * @return Custom analyzers
     */
    List<IndexSettingsObject> getCustomAnalyzers();

    /**
     * Gets optional custom filters.
     * @return Custom filters
     */
    List<IndexSettingsObject> getCustomFilters();

    /**
     * Gets optional custom char filters.
     * @return Custom char filters
     */
    List<IndexSettingsObject> getCustomCharFilters();

    /**
     * Creates a new index name based on the given alias name.
     * @return New index name
     */
    String createNewIndexName();

    @Override
    default Map<String, Object> toDocument() {
        return toDocument(this);
    }

    /**
     * Converts {@link IndexPresetConfiguration} to elasticsearch index document.
     * @param conf  {@link IndexPresetConfiguration}
     * @return      Elasticsearch index document
     */
    static Map<String, Object> toDocument(IndexPresetConfiguration conf) {
        return DocumentBuilder.withoutId()
                .put(CLASS_NAME_FIELD, conf.getClass().getName())
                .put("indexAlias", conf.getIndexAlias())
                .put("numberOfShards", conf.getNumberOfShards())
                .put("numberOfReplicas", conf.getNumberOfReplicas())
                .put("maxResultWindow", conf.getMaxResultWindow())
                .put("fieldsLimit", conf.getFieldsLimit())
                .put("useCompression", conf.isUseCompression())
                .put("customTokenizers", conf.getCustomTokenizers())
                .put("customAnalyzers", conf.getCustomAnalyzers())
                .put("customCharFilters", conf.getCustomCharFilters())
                .put("customFilters", conf.getCustomFilters())
                .build();
    }
}
