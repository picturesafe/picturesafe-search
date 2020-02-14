/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import java.util.Locale;

public interface FacetResolver {

    boolean isResponsible(String field);

    String resolve(String value, Number numberValue, Locale locale);
}
