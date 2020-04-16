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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getBoolean;
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
    private List<StandardFieldConfiguration> nestedFields;
    private Set<String> copyToFields;
    private FieldConfiguration parent;

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
        this.nestedFields = builder.nestedFields;
        initNestedFields();
    }

    private void initNestedFields() {
        if (CollectionUtils.isNotEmpty(nestedFields)) {
            for (final StandardFieldConfiguration nestedField : nestedFields) {
                nestedField.parent = this;
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
    public List<? extends FieldConfiguration> getNestedFields() {
        return nestedFields;
    }

    @Override
    public FieldConfiguration getNestedField(String name) {
        if (CollectionUtils.isNotEmpty(nestedFields)) {
            for (final FieldConfiguration nestedField : nestedFields) {
                if (nestedField.getName().equals(name)) {
                    return nestedField;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isNestedObject() {
        return getElasticsearchType().equalsIgnoreCase(ElasticsearchType.NESTED.toString());
    }

    @Override
    public Set<String> getCopyToFields() {
        return copyToFields;
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

    public static class Builder {
        private final String name;
        private final String elasticsearchType;
        private boolean sortable;
        private boolean aggregatable;
        private boolean multilingual;
        private String analyzer;
        private boolean withoutIndexing;
        private List<StandardFieldConfiguration> nestedFields;
        private Set<String> copyToFields;

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

        public Builder nestedFields(StandardFieldConfiguration... nestedFields) {
            return nestedFields(Arrays.asList(nestedFields));
        }

        public Builder nestedFields(List<StandardFieldConfiguration> nestedFields) {
            this.nestedFields = nestedFields;
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

            if (CollectionUtils.isNotEmpty(nestedFields) && !(elasticsearchType.equalsIgnoreCase(ElasticsearchType.NESTED.toString()))) {
                throw new IllegalArgumentException("Field type has to be '" + ElasticsearchType.NESTED + "' to set nested fields!");
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

        final Collection<Map<String, Object>> nestedDocuments = getDocuments(document, "nestedFields");
        nestedFields = (nestedDocuments != null)
                ? nestedDocuments.stream().map(doc -> new StandardFieldConfiguration().fromDocument(doc)).collect(Collectors.toList())
                : null;
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
                .append(sortable, that.sortable)
                .append(aggregatable, that.aggregatable)
                .append(multilingual, that.multilingual)
                .append(name, that.name)
                .append(elasticsearchType, that.elasticsearchType)
                .append(analyzer, that.analyzer)
                .append(withoutIndexing, that.withoutIndexing)
                .append(nestedFields, that.nestedFields)
                .append(copyToFields, that.copyToFields)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
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
                .append("copyToFields", copyToFields) //--
                .append("nestedFields", nestedFields) //--
                .append("parent", (parent != null) ? parent.getName() : null) //--
                .toString();
    }
}
