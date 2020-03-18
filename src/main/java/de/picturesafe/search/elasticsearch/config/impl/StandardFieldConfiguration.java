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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Definition of a field that will be stored in the elasticsearch index
 */
public class StandardFieldConfiguration implements FieldConfiguration {

    private String name;
    private String elasticsearchType;
    private boolean copyToFulltext;
    private boolean copyToSuggest;
    private boolean sortable;
    private boolean aggregatable;
    private boolean multilingual;
    private String analyzer;
    private List<StandardFieldConfiguration> nestedFields;
    private Set<String> copyToFields;
    private FieldConfiguration parent;

    private StandardFieldConfiguration(Builder builder) {
        this.name = builder.name;
        this.elasticsearchType = builder.elasticsearchType;
        this.copyToFulltext = builder.copyToFulltext;
        this.copyToSuggest = builder.copyToSuggest;
        this.sortable = builder.sortable;
        this.aggregatable = builder.aggregatable;
        this.multilingual = builder.multilingual;
        this.analyzer = builder.analyzer;
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

    public String getName() {
        return name;
    }

    public String getElasticsearchType() {
        return elasticsearchType;
    }

    public boolean isCopyToFulltext() {
        return copyToFulltext;
    }

    public boolean isSortable() {
        return sortable;
    }

    public boolean isAggregatable() {
        return aggregatable;
    }

    public boolean isMultilingual() {
        return multilingual;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public List<? extends FieldConfiguration> getNestedFields() {
        return nestedFields;
    }

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

    public boolean isNestedObject() {
        return getElasticsearchType().equalsIgnoreCase(ElasticsearchType.NESTED.toString());
    }

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
        private boolean copyToFulltext;
        private boolean copyToSuggest;
        private boolean sortable;
        private boolean aggregatable;
        private boolean multilingual;
        private String analyzer;
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

        public Builder copyToFulltext(boolean copyToFulltext) {
            this.copyToFulltext = copyToFulltext;
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
            this.copyToSuggest = copyToSuggest;
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
            if (this.copyToFields == null) {
                this.copyToFields = new TreeSet<>(copyToFields);
            } else if (copyToFields != null){
                this.copyToFields.addAll(copyToFields);
            } else {
                this.copyToFields = null;
            }
            return this;
        }

        public Builder copyTo(String... copyToFields) {
            return copyTo(Arrays.asList(copyToFields));
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
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("elasticsearchType", elasticsearchType) //--
                .append("copyToFulltext", copyToFulltext) //--
                .append("copyToSuggest", copyToSuggest) //--
                .append("sortable", sortable) //--
                .append("aggregatable", aggregatable) //--
                .append("multilingual", multilingual) //--
                .append("analyzer", analyzer) //--
                .append("nestedFields", nestedFields) //--
                .append("copyToFields", copyToFields) //--
                .append("parent", (parent != null) ? parent.getName() : null) //--
                .toString();
    }
}
