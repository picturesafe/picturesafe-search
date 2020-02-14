/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.facet;

import org.elasticsearch.search.aggregations.AggregationBuilder;

public interface AggregationBuilderFactory {
    AggregationBuilder createAggregationBuilder(String field, int size, String language);
}
