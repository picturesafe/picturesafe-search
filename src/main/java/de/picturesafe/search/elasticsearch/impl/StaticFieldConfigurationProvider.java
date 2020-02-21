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
