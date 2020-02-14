/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import java.util.List;
import java.util.Set;

/**
 * Definition of a field that will be stored in the elasticsearch index
 */
public interface FieldConfiguration {

    String FIELD_NAME_FULLTEXT = "fulltext";
    String FIELD_NAME_SUGGEST = "suggest";

    String getName();

    String getElasticsearchType();

    boolean isCopyToFulltext();

    boolean isSortable();

    boolean isAggregatable();

    boolean isMultilingual();

    String getAnalyzer();

    List<FieldConfiguration> getNestedFields();

    FieldConfiguration getNestedField(String name);

    boolean isNestedObject();

    Set<String> getCopyToFields();
}
