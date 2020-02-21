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
