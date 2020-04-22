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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getBoolean;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocument;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocuments;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getInt;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;

/**
 * Standard implementation of a {@link IndexPresetConfiguration}
 */
public class StandardIndexPresetConfiguration implements IndexPresetConfiguration, Cloneable {

    public static final String DEFAULT_INDEX_NAME_DATE_FORMAT = "yyyyMMdd-HHmmss-SSS";
    public static final int DEFAULT_MAX_RESULT_WINDOW = 500_000;

    private String indexAlias;
    private String indexNamePrefix;
    private int numberOfShards;
    private int numberOfReplicas;
    private int maxResultWindow;
    private String indexNameDateFormat;
    private Integer fieldsLimit;
    private boolean useCompression;
    private boolean defaultAnalyzerEnabled;
    private Map<String, String> defaultAnalyzerCharMappings;
    private List<IndexSettingsObject> customTokenizers;
    private List<IndexSettingsObject> customAnalyzers;

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
        this.defaultAnalyzerEnabled = conf.isDefaultAnalyzerEnabled();
        this.defaultAnalyzerCharMappings = (conf.getDefaultAnalyzerCharMappings() != null) ? new HashMap<>(conf.getDefaultAnalyzerCharMappings()) : null;
        this.customTokenizers = (conf.getCustomTokenizers() != null) ? new ArrayList<>(conf.getCustomTokenizers()) : null;
        this.customAnalyzers = (conf.getCustomAnalyzers() != null) ? new ArrayList<>(conf.getCustomAnalyzers()) : null;
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

    @Override
    public boolean isDefaultAnalyzerEnabled() {
        return defaultAnalyzerEnabled;
    }

    /**
     * Sets if the built-in default analyzer is enabled.
     * If disabled the Elasticsearch standard analyzer will be used.
     * Alternatively, custom analyzers can be configured.
     * @param defaultAnalyzerEnabled  TRUE if the default analyzer should be used
     */
    public void setDefaultAnalyzerEnabled(boolean defaultAnalyzerEnabled) {
        this.defaultAnalyzerEnabled = defaultAnalyzerEnabled;
    }

    @Override
    public Map<String, String> getDefaultAnalyzerCharMappings() {
        return defaultAnalyzerCharMappings;
    }

    /**
     * Sets optional character mappings (e.g. for mapping umlauts to latin character sequences) for the default analyzer.
     * @param defaultAnalyzerCharMappings Optional character mappings
     */
    public void setDefaultAnalyzerCharMappings(Map<String, String> defaultAnalyzerCharMappings) {
        this.defaultAnalyzerCharMappings = defaultAnalyzerCharMappings;
    }

    @Override
    public List<IndexSettingsObject> getCustomTokenizers() {
        return customTokenizers;
    }

    /**
     * Sets optional custom tokenizers.
     * @param customTokenizers Custom tokenizers
     */
    public void setCustomTokenizers(List<IndexSettingsObject> customTokenizers) {
        this.customTokenizers = customTokenizers;
    }

    @Override
    public List<IndexSettingsObject> getCustomAnalyzers() {
        return customAnalyzers;
    }

    /**
     * Sets optional custom analyzers.
     * @param customAnalyzers Custom analyzers
     */
    public void setCustomAnalyzers(List<IndexSettingsObject> customAnalyzers) {
        this.customAnalyzers = customAnalyzers;
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
        defaultAnalyzerEnabled = getBoolean(document, "defaultAnalyzerEnabled");

        final Map<String, Object> doc = getDocument(document, "defaultAnalyzerCharMappings");
        if (doc != null) {
            defaultAnalyzerCharMappings = new HashMap<>(doc.size());
            doc.forEach((k, v) -> defaultAnalyzerCharMappings.put(k, v.toString()));
        }

        Collection<Map<String, Object>> docs = getDocuments(document, "customTokenizers");
        customTokenizers = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
        docs = getDocuments(document, "customAnalyzers");
        customAnalyzers = (docs != null) ? docs.stream().map(d -> new IndexSettingsObject().fromDocument(d)).collect(Collectors.toList()) : null;
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
                .append(defaultAnalyzerEnabled, that.defaultAnalyzerEnabled)
                .append(defaultAnalyzerCharMappings, that.defaultAnalyzerCharMappings)
                .append(customTokenizers, that.customTokenizers)
                .append(customAnalyzers, that.customAnalyzers)
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
                .append("defaultAnalyzerEnabled", defaultAnalyzerEnabled)
                .append("defaultAnalyzerCharMappings", defaultAnalyzerCharMappings)
                .append("customTokenizers", customTokenizers)
                .append("customAnalyzers", customAnalyzers)
                .toString();
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public StandardIndexPresetConfiguration clone() {
        final StandardIndexPresetConfiguration conf
                = new StandardIndexPresetConfiguration(indexAlias, indexNamePrefix, indexNameDateFormat, numberOfShards, numberOfReplicas, maxResultWindow);
        conf.fieldsLimit = fieldsLimit;
        conf.useCompression = useCompression;
        conf.defaultAnalyzerCharMappings = (defaultAnalyzerCharMappings != null) ? new HashMap<>(defaultAnalyzerCharMappings) : null;
        conf.customTokenizers = (customTokenizers != null) ? new ArrayList<>(customTokenizers) : null;
        conf.customAnalyzers = (customAnalyzers != null) ? new ArrayList<>(customAnalyzers) : null;
        return conf;
    }
}
