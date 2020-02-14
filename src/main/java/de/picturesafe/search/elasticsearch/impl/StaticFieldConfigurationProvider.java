/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaticFieldConfigurationProvider implements FieldConfigurationProvider {

    private static final List<Locale> DEFAULT_LOCALES = Arrays.asList(Locale.GERMAN, Locale.ENGLISH);

    private final Map<String, List<FieldConfiguration>> fieldConfigurations;
    private List<Locale> supportedLocales = DEFAULT_LOCALES;

    public StaticFieldConfigurationProvider(Map<String, List<FieldConfiguration>> fieldConfigurations) {
        this.fieldConfigurations = fieldConfigurations;
    }

    @Override
    public List<FieldConfiguration> getFieldConfigurations(String indexAlias) {
        return fieldConfigurations.get(indexAlias);
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    @Override
    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }
}
