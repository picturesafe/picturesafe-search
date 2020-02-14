/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.spring.configuration;

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.impl.StaticFieldConfigurationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
public class DefaultIndexConfiguration {

    @Value("${elasticsearch.index.alias:default}")
    private String indexAlias;

    @Value("${elasticsearch.index.name_prefix:default}")
    private String indexNamePrefix;

    @Value("${elasticsearch.index.name_date_format:yyyyMMdd-HHmmss-SSS}")
    private String indexNameDateFormat;

    @Value("${elasticsearch.index.number_of_shards:1}")
    private int numberOfShards;

    @Value("${elasticsearch.index.number_of_replicas:0}")
    private int numberOfReplicas;

    @Value("${elasticsearch.index.fields_limit:1000}")
    private int fieldsLimit;

    @Value("${elasticsearch.index.max_result_window:10000}")
    private int maxResultWindow;

    @Bean
    public StandardIndexPresetConfiguration indexPresetConfiguration() {
        final StandardIndexPresetConfiguration cfg = new StandardIndexPresetConfiguration(indexAlias, indexNamePrefix,
                indexNameDateFormat, numberOfShards, numberOfReplicas, maxResultWindow);
        cfg.setFieldsLimit(fieldsLimit);
        cfg.setCharMappings(defaultCharMapping());
        return cfg;
    }

    @Bean
    FieldConfigurationProvider fieldConfigurationProvider(IndexPresetConfiguration indexPresetConfiguration,
                                                          List<FieldConfiguration> fieldConfigurations) {
        final Map<String, List<FieldConfiguration>> fieldConfigurationMap = new HashMap<>();
        fieldConfigurationMap.put(indexPresetConfiguration.getIndexAlias(), fieldConfigurations);
        return new StaticFieldConfigurationProvider(fieldConfigurationMap);
    }

    protected Map<String, String> defaultCharMapping() {
        final Map<String, String> charMapping = new HashMap<>();
        charMapping.put("ä", "ae");
        charMapping.put("ö", "oe");
        charMapping.put("ü", "ue");
        charMapping.put("ß", "ss");
        charMapping.put("Ä", "Ae");
        charMapping.put("Ö", "Oe");
        charMapping.put("Ü", "Ue");
        return charMapping;
    }
}
