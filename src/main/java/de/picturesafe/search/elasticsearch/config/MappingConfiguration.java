/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Definition of an elasticsearch index mapping
 */
public class MappingConfiguration {

    private final List<FieldConfiguration> fieldConfigurations;
    private List<LanguageSortConfiguration> languageSortConfigurations;

    private Map<String, FieldConfiguration> fieldConfigurationMap = new HashMap<>();
    private Set<String> sortableFieldNames = new HashSet<>();

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
        this.sortableFieldNames = getSortableFieldNames(fieldConfigurations);
    }

    private Set<String> getSortableFieldNames(List<FieldConfiguration> fieldConfigurations) {
        final Set<String> sortableFieldNames = new HashSet<>();

        for (final FieldConfiguration fieldConfig : fieldConfigurations) {
            if (fieldConfig.isSortable()) {
                sortableFieldNames.add(fieldConfig.getName());
            }
            if (fieldConfig.isNestedObject()) {
                for (final FieldConfiguration nestedField : fieldConfig.getNestedFields()) {
                    if (nestedField.isSortable()) {
                        sortableFieldNames.add(fieldConfig.getName() + "." + nestedField.getName());
                    }
                }
            }
        }
        return sortableFieldNames;
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

    /**
     * Gets the sortable field names
     * @return Sortable field names
     */
    public Set<String> getSortableFieldNames() {
        return sortableFieldNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("fieldConfigurations", fieldConfigurations) //--
                .append("languageSortConfigurations", languageSortConfigurations) //--
                .append("fieldConfigurationMap", fieldConfigurationMap) //--
                .append("sortableFieldNames", sortableFieldNames) //--
                .toString();
    }
}
