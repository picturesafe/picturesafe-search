/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;

public interface FilterFactory {

    /**
     * Creates a List of {@link QueryBuilder} (filter).
     *
     * @param queryDto                  {@link QueryDto}
     * @param mappingConfiguration      {@link MappingConfiguration}
     * @return                          A List of {@link QueryBuilder} (filter)
     */
    List<QueryBuilder> create(QueryDto queryDto, MappingConfiguration mappingConfiguration);


    /**
     * Checks if the factory can handle the given search query.
     *
     * @param queryDto                  {@link QueryDto}
     * @param mappingConfiguration      {@link MappingConfiguration}
     * @return                          true if the service can handle the given search query
     */
    boolean canHandleSearch(QueryDto queryDto, MappingConfiguration mappingConfiguration);
}
