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

package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.KEYWORD_FIELD;
import static de.picturesafe.search.elasticsearch.connect.mapping.MappingConstants.MULTILINGUAL_KEYWORD_FIELD;

@SuppressWarnings("unused")
public class FieldConfigurationUtils {

    public static FieldConfiguration fieldConfiguration(List<? extends FieldConfiguration> fieldConfigurations, String elasticFieldName) {
        if (CollectionUtils.isEmpty(fieldConfigurations)) {
            return null;
        }

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

    public static String keywordFieldName(FieldConfiguration fieldConfig, String fieldName, Object... values) {
        final boolean isTextField;
        if (fieldConfig == null) {
            isTextField = ArrayUtils.isNotEmpty(values) && values[0] instanceof String;
        } else {
            isTextField = isTextField(fieldConfig);
        }
        return isTextField ? fieldName + "." + KEYWORD_FIELD : fieldName;
    }

    public static String keywordFieldName(FieldConfiguration fieldConfig, String fieldName) {
        return (fieldConfig != null && isTextField(fieldConfig)) ? fieldName + "." + KEYWORD_FIELD : fieldName;
    }

    public static String sortFieldName(FieldConfiguration fieldConfig, String fieldName) {
        final String sortField = (fieldConfig != null && fieldConfig.isMultilingual()) ? MULTILINGUAL_KEYWORD_FIELD : KEYWORD_FIELD;
        return (fieldConfig != null && isTextField(fieldConfig)) ? fieldName + "." + sortField : fieldName;
    }

    public static boolean isTextField(FieldConfiguration fieldConfig) {
        Validate.notNull(fieldConfig, "Parameter 'fieldConfig' may not be null!");
        return fieldConfig.getElasticsearchType().equalsIgnoreCase(ElasticsearchType.TEXT.toString());
    }

    public static String rootFieldName(FieldConfiguration fieldConfig) {
        return StringUtils.substringBefore(fieldConfig.getName(), ".");
    }
}
