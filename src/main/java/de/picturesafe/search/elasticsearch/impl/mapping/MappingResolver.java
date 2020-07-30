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

package de.picturesafe.search.elasticsearch.impl.mapping;

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.SuggestFieldConfiguration;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class MappingResolver {

    private static final Set<String> KNOWN_PARAMETERS
            = new HashSet<>(Arrays.asList("properties", "type", "nested", "completion", "analyzer", "copy_to", "fields"));

    public static MappingFields resolveFields(Map<String, Object> mapping, String indexName) {
        final List<FieldConfiguration> fieldConfigurations = new ArrayList<>();
        final Set<Locale> locales = new HashSet<>();

        final Map<String, Object> properties = objectValue(mapping, "properties");
        if (MapUtils.isEmpty(properties)) {
            return new MappingFields(Collections.emptyList(), Collections.emptyList());
        }

        properties.forEach((name, value) -> {
            final MappingField field = resolveField(name, (Map<String, Object>) value);
            fieldConfigurations.add(field.getFieldConfiguration());
            locales.addAll(field.getLocales());
        });
        return new MappingFields(fieldConfigurations, new ArrayList<>(locales));
    }

    private static MappingField resolveField(String name, Map<String, Object> properties) {
        final String type = stringValue(properties, "type");
        final boolean enabled = booleanValue(properties, "enabled", true);
        final FieldConfiguration fieldConfiguration;

        if (type == null) {
            if (!enabled) {
                fieldConfiguration = StandardFieldConfiguration.builder(name, ElasticsearchType.OBJECT).withoutIndexing().build();
            } else {
                return resolveObjectField(name, properties);
            }
        } else if (type.equals("nested")) {
            return resolveNestedField(name, properties);
        } else if (type.equals("completion")) {
            fieldConfiguration = SuggestFieldConfiguration.name(name);
        } else {
            fieldConfiguration = fieldConfiguration(name, type, properties, enabled);
        }
        return new MappingField(fieldConfiguration, Collections.emptyList());
    }

    private static FieldConfiguration fieldConfiguration(String name, String type, Map<String, Object> properties, boolean enabled) {
        final boolean sortableAndAggregatable = isSortableAndAggregatable(properties);
        return StandardFieldConfiguration.builder(name, type)
                    .sortable(sortableAndAggregatable)
                    .aggregatable(sortableAndAggregatable)
                    .analyzer(stringValue(properties, "analyzer"))
                    .copyTo((Collection<String>) properties.get("copy_to"))
                    .additionalParameters(additionalParameters(properties))
                    .withoutIndexing(!enabled)
                    .build();
    }

    private static MappingField resolveObjectField(String name, Map<String, Object> doc) {
        final List<Locale> locales = new ArrayList<>();
        final MutableObject<FieldConfiguration> mutableFieldConfig = new MutableObject<>();
        objectValue(doc, "properties").forEach((subName, properties) -> {
            final Locale locale = locale(subName);
            if (locale != null) {
                locales.add(locale);
            }
            final MappingField field = resolveField(subName, (Map<String, Object>) properties);
            if (mutableFieldConfig.getValue() == null) {
                mutableFieldConfig.setValue(field.getFieldConfiguration());
            } else if (!field.getFieldConfiguration().equalsBesidesName(mutableFieldConfig.getValue())) {
                throw new RuntimeException("Object fields are only supported as multilingual text fields or with enabled=false!");
            }
        });
        final FieldConfiguration fieldConfiguration = StandardFieldConfiguration.builder(name, mutableFieldConfig.getValue()).multilingual(true).build();
        return new MappingField(fieldConfiguration, locales);
    }

    private static Locale locale(String name) {
        try {
            return LocaleUtils.toLocale(name);
        } catch (Exception e) {
            return null;
        }
    }

    private static MappingField resolveNestedField(String name, Map<String, Object> doc) {
        final List<StandardFieldConfiguration> nestedFields = new ArrayList<>();
        objectValue(doc, "properties").forEach((subName, properties) -> {
            final MappingField field = resolveField(subName, (Map<String, Object>) properties);
            final FieldConfiguration fieldConfiguration = field.getFieldConfiguration();
            if (fieldConfiguration instanceof StandardFieldConfiguration) {
                nestedFields.add((StandardFieldConfiguration) fieldConfiguration);
            } else {
                throw new RuntimeException(("Unsupported inner field configuration for nested field: " + fieldConfiguration));
            }
        });
        final FieldConfiguration fieldConfiguration
                = StandardFieldConfiguration.builder(name, ElasticsearchType.NESTED).nestedFields(nestedFields).build();
        return new MappingField(fieldConfiguration, Collections.emptyList());
    }

    private static boolean isSortableAndAggregatable(Map<String, Object> properties) {
        if (!properties.get("type").equals("text")) {
            return true;
        }

        final Map<String, Object> fields = objectValue(properties, "fields");
        return MapUtils.isNotEmpty(fields) && (fields.containsKey("keyword") || fields.containsKey("keyword_icu"));
    }

    private static Map<String, Object> objectValue(Map<String, Object> properties, String name) {
        return (Map<String, Object>) properties.get(name);
    }

    private static String stringValue(Map<String, Object> properties, String name) {
        return (String) properties.get(name);
    }

    private static boolean booleanValue(Map<String, Object> properties, String name, boolean defaultValue) {
        final Boolean value = (Boolean) properties.get(name);
        return (value != null) ? value : defaultValue;
    }

    private static Map<String, Object> additionalParameters(Map<String, Object> properties) {
        return properties.entrySet().stream().filter(MappingResolver::additionalParameter).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static boolean additionalParameter(Map.Entry<String, Object> entry) {
        return !KNOWN_PARAMETERS.contains(entry.getKey());
    }
}
