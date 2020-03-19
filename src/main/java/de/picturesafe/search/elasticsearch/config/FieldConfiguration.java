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

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.SuggestFieldConfiguration;

import java.util.List;
import java.util.Set;

/**
 * Definition of a field that will be stored in the elasticsearch index
 */
public interface FieldConfiguration {

    String FIELD_NAME_ID = "id";
    String FIELD_NAME_FULLTEXT = "fulltext";
    String FIELD_NAME_SUGGEST = "suggest";

    FieldConfiguration ID_FIELD = StandardFieldConfiguration.builder(FIELD_NAME_ID, ElasticsearchType.LONG).sortable(true).build();
    FieldConfiguration FULLTEXT_FIELD = StandardFieldConfiguration.builder(FIELD_NAME_FULLTEXT, ElasticsearchType.TEXT).build();
    FieldConfiguration SUGGEST_FIELD = new SuggestFieldConfiguration(FIELD_NAME_SUGGEST);

    String getName();

    String getElasticsearchType();

    boolean isCopyToFulltext();

    boolean isSortable();

    boolean isAggregatable();

    boolean isMultilingual();

    String getAnalyzer();

    List<? extends FieldConfiguration> getNestedFields();

    FieldConfiguration getNestedField(String name);

    boolean isNestedObject();

    Set<String> getCopyToFields();

    FieldConfiguration getParent();
}
