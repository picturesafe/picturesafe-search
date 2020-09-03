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
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.IndexObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.NESTED;

/**
 * Definition of a field that will be stored in the elasticsearch index
 */
public interface FieldConfiguration extends IndexObject<FieldConfiguration> {

    String FIELD_NAME_ID = "id";
    String FIELD_NAME_FULLTEXT = "fulltext";
    String FIELD_NAME_SUGGEST = "suggest";

    FieldConfiguration ID_FIELD = StandardFieldConfiguration.builder(FIELD_NAME_ID, ElasticsearchType.TEXT).sortable(true).build();
    FieldConfiguration FULLTEXT_FIELD = StandardFieldConfiguration.builder(FIELD_NAME_FULLTEXT, ElasticsearchType.TEXT).build();
    FieldConfiguration SUGGEST_FIELD = SuggestFieldConfiguration.name(FIELD_NAME_SUGGEST);

    String getName();

    String getElasticsearchType();

    boolean isCopyToFulltext();

    boolean isSortable();

    boolean isAggregatable();

    boolean isMultilingual();

    String getAnalyzer();

    boolean isWithoutIndexing();

    Set<String> getCopyToFields();

    List<? extends FieldConfiguration> getInnerFields();

    FieldConfiguration getInnerField(String name);

    Map<String, Object> getAdditionalParameters();

    default boolean hasInnerFields() {
        return CollectionUtils.isNotEmpty(getInnerFields());
    }

    default boolean isNestedObject() {
        return getElasticsearchType().equalsIgnoreCase(NESTED.toString());
    }

    default FieldConfiguration getParent() {
        return null;
    }

    default String getParentName() {
        final FieldConfiguration parent = getParent();
        return (parent != null) ? parent.getName() : null;
    }

    @Override
    default Map<String, Object> toDocument() {
        // ATTENTION: Do not add parent here, parent is meant to be transient!
        return DocumentBuilder.withoutId()
                .put(CLASS_NAME_FIELD, getClass().getName())
                .put("name", getName())
                .put("elasticsearchType", getElasticsearchType())
                .put("copyToFulltext", isCopyToFulltext())
                .put("sortable", isSortable())
                .put("aggregatable", isAggregatable())
                .put("multilingual", isMultilingual())
                .put("analyzer", getAnalyzer())
                .put("withoutIndexing", isWithoutIndexing())
                .put("copyToFields", getCopyToFields())
                .put("innerFields", getInnerFields())
                .put("additionalParameters", getAdditionalParameters())
                .build();
    }

    default boolean equalsBesidesName(FieldConfiguration conf) {
        return new EqualsBuilder()
                .append(getElasticsearchType(), conf.getElasticsearchType())
                .append(isCopyToFulltext(), conf.isCopyToFulltext())
                .append(isSortable(), conf.isSortable())
                .append(isAggregatable(), conf.isAggregatable())
                .append(isMultilingual(), conf.isMultilingual())
                .append(getAnalyzer(), conf.getAnalyzer())
                .append(isWithoutIndexing(), conf.isWithoutIndexing())
                .append(getCopyToFields(), conf.getCopyToFields())
                .append(getInnerFields(), conf.getInnerFields())
                .append(getAdditionalParameters(), conf.getAdditionalParameters())
                .append(getParentName(), conf.getParentName())
                .isEquals();
    }
}
