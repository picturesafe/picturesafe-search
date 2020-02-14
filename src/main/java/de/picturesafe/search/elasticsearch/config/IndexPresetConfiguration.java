/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import java.util.List;
import java.util.Map;

/**
 * Presets for index creation
 */
public interface IndexPresetConfiguration {

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
     * Gets optional character mappings (e.g. for mapping umlauts to latin character sequences).
     * @return Optional character mappings
     */
    Map<String, String> getCharMappings();

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
     * Creates a new index name based on the given alias name.
     * @return New index name
     */
    String createNewIndexName();
}
