/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SuggestFieldConfiguration implements FieldConfiguration {

    private String name;
    private String elasticsearchType;

    public SuggestFieldConfiguration() {
        this(FieldConfiguration.FIELD_NAME_SUGGEST);
    }

    public SuggestFieldConfiguration(String name) {
        Validate.isTrue(!name.contains("."), "Parameter 'name' must not contain a '.'!");
        this.name = name;
        this.elasticsearchType = ElasticsearchType.COMPLETION.toString();
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
}
