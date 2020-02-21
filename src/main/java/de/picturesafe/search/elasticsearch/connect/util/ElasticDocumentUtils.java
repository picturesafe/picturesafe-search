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
