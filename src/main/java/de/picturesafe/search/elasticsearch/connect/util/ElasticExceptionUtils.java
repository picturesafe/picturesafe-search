/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import de.picturesafe.search.elasticsearch.connect.error.ElasticExceptionCause;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;

public class ElasticExceptionUtils {

    private ElasticExceptionUtils() {

    }

    public static ElasticExceptionCause getCause(Exception e) {
        if (e.getCause() instanceof ElasticsearchStatusException) {
            final ElasticsearchStatusException ese = (ElasticsearchStatusException) e.getCause();
            final Throwable[] suppressedThrowables = ese.getSuppressed();
            for (Throwable suppressedThrowable : suppressedThrowables) {
                final String suppressedMessage = suppressedThrowable.getMessage();
                if (suppressedMessage.contains("Failed to parse query")) {
                    final String invalidQueryString
                            = StringUtils.substringBetween(suppressedMessage, "Failed to parse query [", "]");
                    return new ElasticExceptionCause(ElasticExceptionCause.Type.QUERY_SYNTAX, invalidQueryString);
                }
            }
        }
        return new ElasticExceptionCause(ElasticExceptionCause.Type.COMMON, "");
    }
}
