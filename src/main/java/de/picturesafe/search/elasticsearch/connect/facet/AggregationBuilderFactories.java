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

package de.picturesafe.search.elasticsearch.connect.facet;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import org.apache.commons.collections.CollectionUtils;

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
        if (CollectionUtils.isEmpty(aggregationBuilderFactories)) {
            final FieldConfiguration fieldConfig = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration.getFieldConfigurations(), fieldName);
            aggregationBuilderFactories = (fieldConfig != null) ? typeAggregationBuilderFactories.get(fieldConfig.getElasticsearchType()) : null;
        }
        return (aggregationBuilderFactories != null) ? aggregationBuilderFactories : Collections.emptyList();
    }
}
