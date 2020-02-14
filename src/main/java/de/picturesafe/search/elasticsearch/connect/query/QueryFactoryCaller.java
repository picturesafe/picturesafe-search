/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.expression.Expression;

import org.elasticsearch.index.query.QueryBuilder;

public interface QueryFactoryCaller {

    QueryBuilder createQuery(Expression expression, MappingConfiguration mappingConfiguration);
}
