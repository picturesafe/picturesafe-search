/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

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
    private List<FieldConfiguration> nestedFields;
    private Set<String> copyToFields;

    private StandardFieldConfiguration(Builder builder) {
        this.name = builder.name;
        this.elasticsearchType = builder.elasticsearchType;
        this.copyToFulltext = builder.copyToFulltext;
        this.copyToSuggest = builder.copyToSuggest;
        this.sortable = builder.sortable;
        this.aggregatable = builder.aggregatable;
        this.multilingual = builder.multilingual;
        this.analyzer = builder.analyzer;
        this.nestedFields = builder.nestedFields;
        this.copyToFields = builder.copyToFields;
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

    public List<FieldConfiguration> getNestedFields() {
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
        private List<FieldConfiguration> nestedFields;
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

        public Builder nestedFields(List<FieldConfiguration> nestedFields) {
            this.nestedFields = nestedFields;
            return this;
        }

        public FieldConfiguration build() {
            final StandardFieldConfiguration fieldConfiguration = new StandardFieldConfiguration(this);
            validateFieldConfiguration(fieldConfiguration);
            return fieldConfiguration;
        }

        private void validateFieldConfiguration(StandardFieldConfiguration fieldConfiguration) {
            Validate.notEmpty(fieldConfiguration.name, "Parameter 'name' must not be empty!");
            Validate.isTrue(!name.contains("."), "Parameter 'name' must not contain a '.'!");
            Validate.notNull(fieldConfiguration.elasticsearchType, "Parameter 'elasticsearchType' must not be null!");

            if (CollectionUtils.isNotEmpty(nestedFields) && !(elasticsearchType.equalsIgnoreCase(ElasticsearchType.NESTED.toString()))) {
                throw new IllegalArgumentException("Field type has to be '" + elasticsearchType + "' to set nested fields!");
            }
        }
    }

    @Override
    public String toString() {
        return "StandardFieldConfiguration{"
                + "name='" + name + '\''
                + ", elasticsearchType=" + elasticsearchType
                + ", copyToFulltext=" + copyToFulltext
                + ", copyToSuggest=" + copyToSuggest
                + ", sortable=" + sortable
                + ", aggregatable=" + aggregatable
                + ", multilingual=" + multilingual
                + ", analyzer='" + analyzer + '\''
                + ", nestedFields=" + nestedFields
                + ", copyToFields=" + copyToFields
                + '}';
    }
}
