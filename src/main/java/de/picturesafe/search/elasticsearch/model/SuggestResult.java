/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
