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

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocument;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;

public class SuggestFieldConfiguration implements FieldConfiguration {

    private String name;
    private String elasticsearchType;
    private Map<String, Object> additionalParameters;

    /**
     * ONLY FOR INTERNAL USAGE
     */
    public SuggestFieldConfiguration() {
        elasticsearchType = ElasticsearchType.COMPLETION.toString();
    }

    private SuggestFieldConfiguration(String name) {
        this();
        Validate.isTrue(!name.contains("."), "Parameter 'name' must not contain a '.'!");
        this.name = name;
    }

    public static SuggestFieldConfiguration name(String name) {
        return new SuggestFieldConfiguration(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getElasticsearchType() {
        return elasticsearchType;
    }

    public boolean isCopyToFulltext() {
        return false;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    public boolean isAggregatable() {
        return false;
    }

    @Override
    public boolean isMultilingual() {
        return false;
    }

    @Override
    public String getAnalyzer() {
        return null;
    }

    @Override
    public boolean isWithoutIndexing() {
        return false;
    }

    @Override
    public List<FieldConfiguration> getNestedFields() {
        return Collections.emptyList();
    }

    @Override
    public FieldConfiguration getNestedField(String name) {
        return null;
    }

    @Override
    public boolean isNestedObject() {
        return false;
    }

    @Override
    public Set<String> getCopyToFields() {
        return null;
    }

    @Override
    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    public SuggestFieldConfiguration additionalParameters(Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
        return this;
    }

    public SuggestFieldConfiguration additionalParameter(String name, Object value) {
        if (additionalParameters == null) {
            additionalParameters = new TreeMap<>();
        }
        additionalParameters.put(name, value);
        return this;
    }

    @Override
    public FieldConfiguration getParent() {
        return null;
    }

    @Override
    public FieldConfiguration fromDocument(Map<String, Object> document) {
        name = getString(document, "name");
        elasticsearchType = getString(document, "elasticsearchType");
        additionalParameters = getDocument(document, "additionalParameters");
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

        final SuggestFieldConfiguration that = (SuggestFieldConfiguration) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(elasticsearchType, that.elasticsearchType)
                .append(additionalParameters, that.additionalParameters)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
