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
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.NESTED;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.OBJECT;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getBoolean;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocument;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getDocuments;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getStringSet;

/**
 * Definition of a field that will be stored in the elasticsearch index
 */
public class StandardFieldConfiguration implements FieldConfiguration {

    private String name;
    private String elasticsearchType;
    private boolean sortable;
    private boolean aggregatable;
    private boolean multilingual;
    private String analyzer;
    private boolean withoutIndexing;
    private List<StandardFieldConfiguration> innerFields;
    private Set<String> copyToFields;
    private Map<String, Object> additionalParameters;

    private transient FieldConfiguration parent;

    /**
     * ONLY FOR INTERNAL USAGE
     */
    public StandardFieldConfiguration() {
    }

    private StandardFieldConfiguration(Builder builder) {
        this.name = builder.name;
        this.elasticsearchType = builder.elasticsearchType;
        this.sortable = builder.sortable;
        this.aggregatable = builder.aggregatable;
        this.multilingual = builder.multilingual;
        this.analyzer = builder.analyzer;
        this.withoutIndexing = builder.withoutIndexing;
        this.copyToFields = builder.copyToFields;
        this.additionalParameters = builder.additionalParameters;
        this.innerFields = builder.innerFields;
        initInnerFields();
    }

    private void initInnerFields() {
        if (CollectionUtils.isNotEmpty(innerFields)) {
            for (final StandardFieldConfiguration innerField : innerFields) {
                innerField.parent = this;
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getElasticsearchType() {
        return elasticsearchType;
    }

    @Override
    public boolean isCopyToFulltext() {
        return copyToFields != null && copyToFields.contains(FIELD_NAME_FULLTEXT);
    }

    @Override
    public boolean isSortable() {
        return sortable;
    }

    @Override
    public boolean isAggregatable() {
        return aggregatable;
    }

    @Override
    public boolean isMultilingual() {
        return multilingual;
    }

    @Override
    public String getAnalyzer() {
        return analyzer;
    }

    @Override
    public boolean isWithoutIndexing() {
        return withoutIndexing;
    }

    @Override
    public List<? extends FieldConfiguration> getInnerFields() {
        return innerFields;
    }

    @Override
    public FieldConfiguration getInnerField(String name) {
        if (hasInnerFields()) {
            for (final FieldConfiguration innerField : innerFields) {
                if (innerField.getName().equals(name)) {
                    return innerField;
                }
            }
        }
        return null;
    }

    @Override
    public Set<String> getCopyToFields() {
        return copyToFields;
    }

    @Override
    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    @Override
    public FieldConfiguration getParent() {
        return parent;
    }

    public static Builder builder(String name, ElasticsearchType elasticsearchType) {
        return new Builder(name, elasticsearchType);
    }

    public static Builder builder(String name, String elasticsearchType) {
        return new Builder(name, elasticsearchType);
    }

    public static Builder builder(String name, FieldConfiguration fieldConfiguration) {
        final Builder builder = new Builder(name, fieldConfiguration.getElasticsearchType());
        builder.sortable = fieldConfiguration.isSortable();
        builder.aggregatable = fieldConfiguration.isAggregatable();
        builder.multilingual = fieldConfiguration.isMultilingual();
        builder.analyzer = fieldConfiguration.getAnalyzer();
        builder.withoutIndexing = fieldConfiguration.isWithoutIndexing();
        builder.copyToFields = fieldConfiguration.getCopyToFields();
        builder.additionalParameters = fieldConfiguration.getAdditionalParameters();
        return builder;
    }

    public static class Builder {
        private final String name;
        private final String elasticsearchType;
        private boolean sortable;
        private boolean aggregatable;
        private boolean multilingual;
        private String analyzer;
        private boolean withoutIndexing;
        private List<StandardFieldConfiguration> innerFields;
        private Set<String> copyToFields;
        private Map<String, Object> additionalParameters = new TreeMap<>();;

        public Builder(String name, ElasticsearchType elasticsearchType) {
            this.name = name;
            this.elasticsearchType = elasticsearchType.toString();
        }

        public Builder(String name, String elasticsearchType) {
            this.name = name;
            this.elasticsearchType = elasticsearchType;
        }

        public Builder sortable(boolean sortable) {
            this.sortable = sortable;
            return this;
        }

        public Builder aggregatable(boolean aggregatable) {
            this.aggregatable = aggregatable;
            return this;
        }

        public Builder multilingual(boolean multilingual) {
            this.multilingual = multilingual;
            return this;
        }

        public Builder analyzer(String analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public Builder withoutIndexing() {
            this.withoutIndexing = true;
            return this;
        }

        public Builder withoutIndexing(boolean withoutIndexing) {
            this.withoutIndexing = withoutIndexing;
            return this;
        }

        public Builder copyToFulltext(boolean copyToFulltext) {
            if (copyToFulltext) {
                if (copyToFields == null) {
                    copyToFields = new TreeSet<>();
                }
                copyToFields.add(FIELD_NAME_FULLTEXT);
            } else if (copyToFields != null) {
                copyToFields.remove(FIELD_NAME_FULLTEXT);
            }
            return this;
        }

        public Builder copyToSuggest(boolean copyToSuggest) {
            if (copyToSuggest) {
                if (copyToFields == null) {
                    copyToFields = new TreeSet<>();
                }
                copyToFields.add(FIELD_NAME_SUGGEST);
            } else if (copyToFields != null) {
                copyToFields.remove(FIELD_NAME_SUGGEST);
            }
            return this;
        }

        public Builder copyTo(Collection<String> copyToFields) {
            if (copyToFields == null) {
                this.copyToFields = null;
            } else if (this.copyToFields == null) {
                this.copyToFields = new TreeSet<>(copyToFields);
            } else {
                this.copyToFields.addAll(copyToFields);
            }
            return this;
        }

        public Builder copyTo(String... copyToFields) {
            return copyTo(Arrays.asList(copyToFields));
        }

        public Builder innerFields(StandardFieldConfiguration... innerFields) {
            return innerFields(Arrays.asList(innerFields));
        }

        @SuppressWarnings("unchecked")
        public Builder innerFields(List<? extends FieldConfiguration> innerFields) {
            Validate.isTrue(innerFields.stream().allMatch(f -> f instanceof StandardFieldConfiguration),
                    "Inner fields support only instances of StandardFieldConfiguration!");
            this.innerFields = (List<StandardFieldConfiguration>) innerFields;
            return this;
        }

        public Builder additionalParameters(Map<String, Object> additionalParameters) {
            this.additionalParameters = additionalParameters;
            return this;
        }

        public Builder additionalParameter(String name, Object value) {
            additionalParameters.put(name, value);
            return this;
        }

        public StandardFieldConfiguration build() {
            final StandardFieldConfiguration fieldConfiguration = new StandardFieldConfiguration(this);
            validateFieldConfiguration(fieldConfiguration);
            return fieldConfiguration;
        }

        private void validateFieldConfiguration(StandardFieldConfiguration fieldConfiguration) {
            Validate.notEmpty(fieldConfiguration.name, "Parameter 'name' must not be empty!");
            Validate.isTrue(!name.contains("."), "Parameter 'name' must not contain a '.'!");
            Validate.notNull(fieldConfiguration.elasticsearchType, "Parameter 'elasticsearchType' must not be null!");

            if (CollectionUtils.isNotEmpty(innerFields)) {
                Validate.isTrue(elasticsearchType.equals(NESTED.getElasticType()) || elasticsearchType.equals(OBJECT.getElasticType()),
                        "Inner fields are only supported by elasticsearch types '%s' or '%s'!", NESTED, OBJECT);
            }
        }
    }

    @Override
    public StandardFieldConfiguration fromDocument(Map<String, Object> document) {
        name = getString(document, "name");
        elasticsearchType = getString(document, "elasticsearchType");
        sortable = getBoolean(document, "sortable");
        aggregatable = getBoolean(document, "aggregatable");
        multilingual = getBoolean(document, "multilingual");
        analyzer = getString(document, "analyzer");
        withoutIndexing = getBoolean(document, "withoutIndexing");
        copyToFields = getStringSet(document, "copyToFields");
        additionalParameters = getDocument(document, "additionalParameters");

        final Collection<Map<String, Object>> nestedDocuments = getDocuments(document, "innerFields");
        innerFields = (nestedDocuments != null)
                ? nestedDocuments.stream().map(doc -> new StandardFieldConfiguration().fromDocument(doc)).collect(Collectors.toList())
                : null;
        initInnerFields();

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

        final StandardFieldConfiguration that = (StandardFieldConfiguration) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(elasticsearchType, that.elasticsearchType)
                .append(sortable, that.sortable)
                .append(aggregatable, that.aggregatable)
                .append(multilingual, that.multilingual)
                .append(withoutIndexing, that.withoutIndexing)
                .append(analyzer, that.analyzer)
                .append(innerFields, that.innerFields)
                .append(copyToFields, that.copyToFields)
                .append(additionalParameters, that.additionalParameters)
                .append(getParentName(), that.getParentName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(elasticsearchType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("elasticsearchType", elasticsearchType) //--
                .append("sortable", sortable) //--
                .append("aggregatable", aggregatable) //--
                .append("multilingual", multilingual) //--
                .append("analyzer", analyzer) //--
                .append("withoutIndexing", withoutIndexing) //--
                .append("innerFields", innerFields) //--
                .append("copyToFields", copyToFields) //--
                .append("additionalParameters", additionalParameters) //--
                .append("parent", getParentName()) //--
                .toString();
    }
}
