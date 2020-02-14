/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import java.util.Map;

public class ElasticDocumentUtils {

    private ElasticDocumentUtils() {
    }

    public static long getId(Map<String, Object> doc) {
        final Object val = doc.get("id");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            return Long.parseLong((String) val);
        } else if (val != null) {
            throw new IllegalArgumentException("Document field 'id' has unsupported type: " + val);
        } else {
            throw new NullPointerException("Missing field 'id' in document!");
        }
    }
}
