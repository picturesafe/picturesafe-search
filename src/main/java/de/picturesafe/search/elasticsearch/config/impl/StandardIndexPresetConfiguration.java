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

package de.picturesafe.search.elasticsearch.config.impl;

import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexSettingsObject;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getBoolean;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocuments;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getInt;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;

/**
 * Standard implementation of a {@link IndexPresetConfiguration}
 */
public class StandardIndexPresetConfiguration implements IndexPresetConfiguration, Cloneable {

    public static final String DEFAULT_INDEX_NAME_DATE_FORMAT = "yyyyMMdd-HHmmss-SSS";
    public static final int DEFAULT_MAX_RESULT_WINDOW = 500_000;

    protected static final String CHAR_FILTER_UMLAUT_MAPPING = "umlaut_mapping";
    protected static final String FILTER_WORD_DELIMITER = "filter_word_delimiter";

    private String indexAlias;
    private String indexNamePrefix;
    private int numberOfShards;
    private int numberOfReplicas;
    private int maxResultWindow;
    private String indexNameDateFormat;
    private Integer fieldsLimit;
    private boolean useCompression;
    private List<IndexSettingsObject> customTokenizers = new ArrayList<>();
    private List<IndexSettingsObject> customAnalyzers = new ArrayList<>();
    private List<IndexSettingsObject> customCharFilters = new ArrayList<>();
    private List<IndexSettingsObject> customFilters = new ArrayList<>();

    /**
     * ONLY FOR INTERNAL USAGE
     */
    public StandardIndexPresetConfiguration() {
    }

    /**
     * Constructor
     * @param indexAlias        Alias name
     * @param numberOfShards    Number of shards
     * @param numberOfReplicas  Number of replicas
     */
    public StandardIndexPresetConfiguration(String indexAlias, int numberOfShards, int numberOfReplicas) {
        this(indexAlias, indexAlias, DEFAULT_INDEX_NAME_DATE_FORMAT, numberOfShards, numberOfReplicas, DEFAULT_MAX_RESULT_WINDOW);
    }

    /**
     * Constructor
     * @param indexAlias        Alias name
     * @param numberOfShards    Number of shards
     * @param numberOfReplicas  Number of replicas
     * @param maxResultWindow   Maximum result window size (maximum number of hits in a search result)
     */
    public StandardIndexPresetConfiguration(String indexAlias, int numberOfShards, int numberOfReplicas, int maxResultWindow) {
        this(indexAlias, indexAlias, DEFAULT_INDEX_NAME_DATE_FORMAT, numberOfShards, numberOfReplicas, maxResultWindow);
    }

    /**
     * Constructor
     * @param indexAlias            Alias name
     * @param indexNamePrefix       Index name prefix (<code>indexAlias</code> will be used, when null or empty).
     * @param indexNameDateFormat   Index name date format (<code>"yyyyMMdd-HHmmss-SSS"</code> will be used, when null or empty).
     * @param numberOfShards        Number of shards
     * @param numberOfReplicas      Number of replicas
     * @param maxResultWindow       Maximum result window size (maximum number of hits in a search result)
     */
    public StandardIndexPresetConfiguration(String indexAlias, String indexNamePrefix, String indexNameDateFormat, int numberOfShards, int numberOfReplicas,
                                            int maxResultWindow) {
        Validate.notEmpty(indexAlias, "Argument 'indexAlias' must not be empty!");
        this.indexAlias = indexAlias;
        this.indexNamePrefix = (StringUtils.isEmpty(indexNamePrefix)) ? indexAlias : indexNamePrefix;
        this.indexNameDateFormat = indexNameDateFormat;
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
        this.maxResultWindow = maxResultWindow;
    }

    /**
     * Constructor for cloning another {@link IndexPresetConfiguration}.
     * @param conf          {@link IndexPresetConfiguration to clone}
     * @param indexAlias    New alias name
     */
    public StandardIndexPresetConfiguration(IndexPresetConfiguration conf, String indexAlias) {
        this(indexAlias, indexAlias, DEFAULT_INDEX_NAME_DATE_FORMAT, conf.getNumberOfShards(), conf.getNumberOfReplicas(), conf.getMaxResultWindow());
        this.fieldsLimit = conf.getFieldsLimit();
        this.useCompression = conf.isUseCompression();
        this.customTokenizers = (conf.getCustomTokenizers() != null) ? new ArrayList<>(conf.getCustomTokenizers()) : null;
        this.customAnalyzers = (conf.getCustomAnalyzers() != null) ? new ArrayList<>(conf.getCustomAnalyzers()) : null;
        this.customCharFilters = (conf.getCustomCharFilters() != null) ? new ArrayList<>(conf.getCustomCharFilters()) : null;
        this.customFilters = (conf.getCustomFilters() != null) ? new ArrayList<>(conf.getCustomFilters()) : null;
    }

    /**
     * Sets the index name date format.
     * @param indexNameDateFormat Index name date format
     */
    public void setIndexNameDateFormat(String indexNameDateFormat) {
        this.indexNameDateFormat = indexNameDateFormat;
    }

    @Override
    public String getIndexAlias() {
        return indexAlias;
    }

    @Override
    public int getNumberOfShards() {
        return numberOfShards;
    }

    @Override
    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }

    @Override
    public int getMaxResultWindow() {
        return maxResultWindow;
    }

    /**
     * Sets the maximum result window size (maximum number of hits in a search result).
     * @param maxResultWindow Maximum result window size
     */
    public void setMaxResultWindow(int maxResultWindow) {
        this.maxResultWindow = maxResultWindow;
    }

    @Override
    public Integer getFieldsLimit() {
        return fieldsLimit;
    }

    /**
     * Sets the maximum number of fields in the index.
     * @param fieldsLimit Maximum number of fields in the index (null = use default)
     */
    public void setFieldsLimit(Integer fieldsLimit) {
        this.fieldsLimit = fieldsLimit;
    }

    @Override
    public boolean isUseCompression() {
        return useCompression;
    }

    /**
     * Sets if compression should be used.
     * @param useCompression TRUE if compression should be used
     */
    public void setUseCompression(boolean useCompression) {
        this.useCompression = useCompression;
    }

    /**
     * Adds default analysis settings.
     * @param charMappings  Char mappings for analysis settings
     */
    public void addDefaultAnalysisSettings(Map<String, String> charMappings) {
        addDefaultCharFilterSettings(charMappings);
        addDefaultFilterSettings();
        addDefaultAnalyzerSettings(charMappings);
    }

    protected void addDefaultCharFilterSettings(Map<String, String> charMappings) {
        try {
            if (charMappings != null) {
                final String[] mappings = new String[charMappings.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : charMappings.entrySet()) {
                    mappings[i++] = entry.getKey() + "=>" + entry.getValue();
                }

                final IndexSettingsObject defaultCharFilter = new IndexSettingsObject(CHAR_FILTER_UMLAUT_MAPPING);
                defaultCharFilter.content().startObject()
                        .field("type", "mapping")
                        .field("mappings", mappings)
                        .endObject();
                customCharFilters.add(defaultCharFilter);
            }
        } catch (Exception e) {
            throw new RuntimeException("Adding default char filter index settings failed!", e);
        }
    }

    protected void addDefaultFilterSettings() {
        try {
            final IndexSettingsObject defaultFilter = new IndexSettingsObject(FILTER_WORD_DELIMITER);
            defaultFilter.content().startObject()
                    .field("type", "word_delimiter_graph")
                    .field("split_on_numerics", false)
                    .field("split_on_case_change", false)
                    .endObject();
            customFilters.add(defaultFilter);
        } catch (Exception e) {
            throw new RuntimeException("Adding default filter index settings failed!", e);
        }
    }

    protected void addDefaultAnalyzerSettings(Map<String, String> charMappings) {
        try {
            final String[] charFilters = {CHAR_FILTER_UMLAUT_MAPPING};
            final String[] filters = {FILTER_WORD_DELIMITER, "lowercase", "trim"};

            final IndexSettingsObject defaultAnalyzer = new IndexSettingsObject("default");
            final XContentBuilder settings = defaultAnalyzer.content().startObject();
            if (charMappings != null) {
                settings.field("char_filter", charFilters);
            }
            settings.field("tokenizer", "standard")
                    .field("filter", filters);
            settings.endObject();

            customAnalyzers.add(defaultAnalyzer);
        } catch (IOException e) {
            throw new RuntimeException("Adding default analyzer index settings failed!", e);
        }
    }

    @Override
    public List<IndexSettingsObject> getCustomTokenizers() {
        return customTokenizers;
    }


    /**
     * Adds optional custom tokenizers.
     * @param customTokenizers Custom tokenizers
     */
    public void addCustomTokenizers(IndexSettingsObject... customTokenizers) {
        this.customTokenizers.addAll(Arrays.asList(customTokenizers));
    }

    @Override
    public List<IndexSettingsObject> getCustomAnalyzers() {
        return customAnalyzers;
    }

    /**
     * Adds optional custom analyzers.
     * @param customAnalyzers Custom analyzers
     */
    public void addCustomAnalyzers(IndexSettingsObject... customAnalyzers) {
        this.customAnalyzers.addAll(Arrays.asList(customAnalyzers));
    }

    @Override
    public List<IndexSettingsObject> getCustomFilters() {
        return customFilters;
    }

    /**
     * Adds optional custom filters.
     * @param customFilters Custom filters
     */
    public void addCustomFilters(IndexSettingsObject... customFilters) {
        this.customFilters.addAll(Arrays.asList(customFilters));
    }

    @Override
    public List<IndexSettingsObject> getCustomCharFilters() {
        return customCharFilters;
    }

    /**
     * Adds optional custom char filters.
     * @param customCharFilters Custom char filters
     */
    public void addCustomCharFilters(IndexSettingsObject... customCharFilters) {
        this.customCharFilters.addAll(Arrays.asList(customCharFilters));
    }



    @Override
    public String createNewIndexName() {
        return indexNamePrefix + "-" + new SimpleDateFormat(indexNameDateFormat, Locale.GERMAN).format(new Date());
    }

    @Override
    public Map<String, Object> toDocument() {
        final Map<String, Object> document = IndexPresetConfiguration.toDocument(this);
        document.put("indexNamePrefix", indexNamePrefix);
        document.put("indexNameDateFormat", indexNameDateFormat);
        return document;
    }

    @Override
    public IndexPresetConfiguration fromDocument(Map<String, Object> document) {
        indexAlias = getString(document, "indexAlias");
        indexNamePrefix = getString(document, "indexNamePrefix");
        indexNameDateFormat = getString(document, "indexNameDateFormat");
        numberOfShards = getInt(document, "numberOfShards", 1);
        numberOfReplicas = getInt(document, "numberOfReplicas", 0);
        maxResultWindow = getInt(document, "maxResultWindow", DEFAULT_MAX_RESULT_WINDOW);
        useCompression = getBoolean(document, "useCompression");

        Collection<Map<String, Object>> docs = getDocuments(document, "customTokenizers");
        customTokenizers = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
        docs = getDocuments(document, "customAnalyzers");
        customAnalyzers = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
        docs = getDocuments(document, "customFilters");
        customFilters = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
        docs = getDocuments(document, "customCharFilters");
        customCharFilters = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StandardIndexPresetConfiguration that = (StandardIndexPresetConfiguration) o;
        return new EqualsBuilder()
                .append(indexAlias, that.indexAlias)
                .append(indexNamePrefix, that.indexNamePrefix)
                .append(indexNameDateFormat, that.indexNameDateFormat)
                .append(numberOfShards, that.numberOfShards)
                .append(numberOfReplicas, that.numberOfReplicas)
                .append(maxResultWindow, that.maxResultWindow)
                .append(useCompression, that.useCompression)
                .append(customTokenizers, that.customTokenizers)
                .append(customAnalyzers, that.customAnalyzers)
                .append(customFilters, that.customFilters)
                .append(customCharFilters, that.customCharFilters)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return indexAlias.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("indexAlias", indexAlias)
                .append("indexNamePrefix", indexNamePrefix)
                .append("indexNameDateFormat", indexNameDateFormat)
                .append("numberOfShards", numberOfShards)
                .append("numberOfReplicas", numberOfReplicas)
                .append("useCompression", useCompression)
                .append("customTokenizers", customTokenizers)
                .append("customAnalyzers", customAnalyzers)
                .append("customFilters", customFilters)
                .append("customCharFilters", customCharFilters)
                .toString();
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public StandardIndexPresetConfiguration clone() {
        final StandardIndexPresetConfiguration conf
                = new StandardIndexPresetConfiguration(indexAlias, indexNamePrefix, indexNameDateFormat, numberOfShards, numberOfReplicas, maxResultWindow);
        conf.fieldsLimit = fieldsLimit;
        conf.useCompression = useCompression;
        conf.customTokenizers = (customTokenizers != null) ? new ArrayList<>(customTokenizers) : null;
        conf.customAnalyzers = (customAnalyzers != null) ? new ArrayList<>(customAnalyzers) : null;
        conf.customFilters = (customFilters != null) ? new ArrayList<>(customFilters) : null;
        conf.customCharFilters = (customCharFilters != null) ? new ArrayList<>(customCharFilters) : null;
        return conf;
    }
}
