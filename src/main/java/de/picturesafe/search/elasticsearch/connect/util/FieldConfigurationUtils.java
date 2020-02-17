/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.KEYWORD_FIELD;
import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.MULTILINGUAL_KEYWORD_FIELD;

@SuppressWarnings("unused")
public class FieldConfigurationUtils {

    public static FieldConfiguration fieldConfiguration(List<FieldConfiguration> fieldConfigurations, String elasticFieldName) {
        Validate.notEmpty(fieldConfigurations, "Parameter 'fieldConfiguration' may not be null or empty!");

        for (FieldConfiguration fieldConfiguration : fieldConfigurations) {
            if (fieldConfiguration.getName().equals(elasticFieldName)) {
                return fieldConfiguration;
            }
        }
        return null;
    }

    public static FieldConfiguration fieldConfiguration(MappingConfiguration mappingConfiguration, String elasticFieldName) {
        return fieldConfiguration(mappingConfiguration, elasticFieldName, true);
    }

    public static FieldConfiguration fieldConfiguration(MappingConfiguration mappingConfiguration, String elasticFieldName, boolean trimFieldName) {
        Validate.notNull(mappingConfiguration, "Parameter 'indexConfiguration' may not be null!");
        if (trimFieldName) {
            String trimmedFieldName = StringUtils.defaultString(elasticFieldName).trim();
            trimmedFieldName = StringUtils.substringBefore(trimmedFieldName, ".");
            elasticFieldName = trimmedFieldName;
        }
        return mappingConfiguration.getFieldConfiguration(elasticFieldName);
    }

    public static String getElasticFieldName(MappingConfiguration mappingConfiguration, String fieldName, Locale locale) {
        final FieldConfiguration fieldConfig = fieldConfiguration(mappingConfiguration, fieldName);
        if (fieldConfig != null && fieldConfig.isMultilingual() && locale != null) {
            fieldName += "." + locale.getLanguage();
        }
        return fieldName;
    }

    public static String keywordFieldName(FieldConfiguration fieldConfig, String fieldName) {
        return isTextField(fieldConfig) ? fieldName + "." + KEYWORD_FIELD : fieldName;
    }

    public static String sortFieldName(FieldConfiguration fieldConfig, String fieldName) {
        final String sortField = fieldConfig.isMultilingual() ? MULTILINGUAL_KEYWORD_FIELD : KEYWORD_FIELD;
        return isTextField(fieldConfig) ? fieldName + "." + sortField : fieldName;
    }

    public static boolean isTextField(FieldConfiguration fieldConfig) {
        return fieldConfig.getElasticsearchType().equalsIgnoreCase(ElasticsearchType.TEXT.toString());
    }

    public static String rootFieldName(FieldConfiguration fieldConfig) {
        return StringUtils.substringBefore(fieldConfig.getName(), ".");
    }
}
