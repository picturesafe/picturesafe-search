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

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;
import java.util.Map;

/**
 * Result of a suggest search for search-as-you-type functionality.
 */
public class SuggestResult {

    private final Map<String, List<String>> suggestions;

    /**
     * Constructor
     *
     * @param suggestions Suggestions made by Elasticsearch
     */
    public SuggestResult(Map<String, List<String>> suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * Gets the suggestions made by Elasticsearch.
     *
     * @return Suggestions made by Elasticsearch
     */
    public Map<String, List<String>> getSuggestions() {
        return suggestions;
    }

    /**
     * Gets the suggestions for a field.
     *
     * @param fieldName     Name of the suggest field
     * @return              Suggestions made by Elasticsearch
     */
    public List<String> getSuggestions(String fieldName) {
        return suggestions.get(fieldName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("suggestions", suggestions) //--
                .toString();
    }
}
