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
