/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.mock;

import de.picturesafe.search.elasticsearch.connect.FacetResolver;

import java.util.Locale;

public class FacetResolverMock implements FacetResolver {

    @Override
    public boolean isResponsible(String field) {
        return field.equals("facetResolved");
    }

    @Override
    public String resolve(String value, Number numberValue, Locale locale) {
        return value.equals("1") ? "true" : "false";
    }
}
