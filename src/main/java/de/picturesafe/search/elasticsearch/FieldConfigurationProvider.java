/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */
package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;

import java.util.List;
import java.util.Locale;

/**
 * Service interface of a field configuration provider
 */
public interface FieldConfigurationProvider {

    /**
     * Gets the field configurations for an index.
     *
     * @param indexAlias    Name of the alias of the index
     * @return              List of field configurations
     */
    List<FieldConfiguration> getFieldConfigurations(String indexAlias);

    /**
     * Gets the supported locales for multilingual fields.
     *
     * @return Supported locales
     */
    List<Locale> getSupportedLocales();
}
