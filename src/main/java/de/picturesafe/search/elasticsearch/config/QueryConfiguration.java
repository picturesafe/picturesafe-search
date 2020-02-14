/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import org.elasticsearch.index.query.Operator;

/**
 * Configuration presets for query generation.
 */
public class QueryConfiguration {

    private Operator defaultQueryStringOperator = Operator.AND;

    /**
     * Gets the default query string operator.
     * @return Default query string operator
     */
    public Operator getDefaultQueryStringOperator() {
        return defaultQueryStringOperator;
    }

    /**
     * Sets the default query string operator.
     * @param defaultQueryStringOperator Default query string operator
     */
    public void setDefaultQueryStringOperator(Operator defaultQueryStringOperator) {
        this.defaultQueryStringOperator = defaultQueryStringOperator;
    }
}
