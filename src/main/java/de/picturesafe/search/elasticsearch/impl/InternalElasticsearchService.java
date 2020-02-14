/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;

import java.util.List;
import java.util.Map;

public interface InternalElasticsearchService extends ElasticsearchService {

    void addToIndex(MappingConfiguration mappingConfiguration, String indexName, DataChangeProcessingMode dataChangeProcessingMode,
                    List<Map<String, Object>> documents);

    MappingConfiguration getMappingConfiguration(String indexAlias);
}
