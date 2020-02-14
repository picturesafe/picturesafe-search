/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.facet;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregationBuilderFactories {

    private Map<String, List<AggregationBuilderFactory>> fieldAggregationBuilderFactories = new HashMap<>();
    private Map<String, List<AggregationBuilderFactory>> typeAggregationBuilderFactories = new HashMap<>();

    public void setFieldAggregationBuilderFactories(Map<String, List<AggregationBuilderFactory>> fieldAggregationBuilderFactories) {
        this.fieldAggregationBuilderFactories = fieldAggregationBuilderFactories;
    }

    public void setTypeAggregationBuilderFactories(Map<String, List<AggregationBuilderFactory>> typeAggregationBuilderFactories) {
        this.typeAggregationBuilderFactories = typeAggregationBuilderFactories;
    }

    public List<AggregationBuilderFactory> getAggregationBuilderFactories(MappingConfiguration mappingConfiguration, String fieldName) {
        List<AggregationBuilderFactory> aggregationBuilderFactories = fieldAggregationBuilderFactories.get(fieldName);
        if (CollectionUtils.isNotEmpty(aggregationBuilderFactories)) {
            return aggregationBuilderFactories;
        }

        final FieldConfiguration field = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration.getFieldConfigurations(), fieldName);
        Validate.notNull(field, "Failed to find the field with the name '" + fieldName + "' in the elastic field configurations");

        aggregationBuilderFactories = typeAggregationBuilderFactories.get(field.getElasticsearchType());
        return (aggregationBuilderFactories != null) ? aggregationBuilderFactories : Collections.emptyList();
    }
}
