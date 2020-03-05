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

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definition of an elasticsearch index mapping
 */
public class MappingConfiguration {

    private final List<FieldConfiguration> fieldConfigurations;
    private List<LanguageSortConfiguration> languageSortConfigurations;

    private Map<String, FieldConfiguration> fieldConfigurationMap = new HashMap<>();

    /**
     * Constructor
     * @param fieldConfigurations Field configurations
     */
    public MappingConfiguration(List<FieldConfiguration> fieldConfigurations) {
        Validate.notNull(fieldConfigurations, "Parameter 'fieldConfigurations' may not be null!");
        this.fieldConfigurations = fieldConfigurations;
        for (FieldConfiguration fieldConfiguration : fieldConfigurations) {
            fieldConfigurationMap.put(fieldConfiguration.getName(), fieldConfiguration);
            if (CollectionUtils.isNotEmpty(fieldConfiguration.getNestedFields())) {
                for (final FieldConfiguration nestedConfig : fieldConfiguration.getNestedFields()) {
                    fieldConfigurationMap.put(fieldConfiguration.getName() + "." + nestedConfig.getName(), nestedConfig);
                }
            }
        }
    }

    /**
     * Constructor
     * @param fieldConfigurations           Filed configurations
     * @param languageSortConfigurations    Language sort configurations
     */
    public MappingConfiguration(List<FieldConfiguration> fieldConfigurations,
                                List<LanguageSortConfiguration> languageSortConfigurations) {
        this(fieldConfigurations);
        this.languageSortConfigurations = languageSortConfigurations;
    }

    /**
     * Gets the field configurations.
     * @return Field configurations
     */
    public List<FieldConfiguration> getFieldConfigurations() {
        return fieldConfigurations;
    }

    /**
     * Gets the configuration of a specific field.
     * @param fieldName Field name
     * @return          Field configuration or <code>null</code> if the field name is unknown
     */
    public FieldConfiguration getFieldConfiguration(String fieldName) {
        return fieldConfigurationMap.get(fieldName);
    }

    /**
     * Gets the language sort configurations
     * @return Language sort configurations
     */
    public List<LanguageSortConfiguration> getLanguageSortConfigurations() {
        return languageSortConfigurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("fieldConfigurations", fieldConfigurations) //--
                .append("languageSortConfigurations", languageSortConfigurations) //--
                .append("fieldConfigurationMap", fieldConfigurationMap) //--
                .toString();
    }
}
