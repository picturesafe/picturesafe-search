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

import java.util.Date;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils.parseIso;

public class ElasticDocumentUtils {

    private ElasticDocumentUtils() {
    }

    public static long getId(Map<String, Object> doc, long fallbackValue) {
        final Long id = getId(doc);
        return (id != null) ? id : fallbackValue;
    }

    public static Long getId(Map<String, Object> doc) {
        final Object val = doc.get("id");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof String) {
            return Long.parseLong((String) val);
        } else if (val != null) {
            throw new IllegalArgumentException("Document field 'id' has unsupported type: " + val);
        } else {
            return null;
        }
    }

    public static String getString(Map<String, Object> doc, String name) {
        final Object value = doc.get(name);
        return (value != null) ? (String) value : null;
    }

    public static Date getDate(Map<String, Object> doc, String name) {
        final String value = getString(doc, name);
        return (value != null) ? parseIso(value) : null;
    }

    public static long getLong(Map<String, Object> doc, String name, long fallbackValue) {
        final Object value = doc.get(name);
        return (value instanceof Number) ? ((Number) value).longValue() : fallbackValue;
    }

    public static int getInt(Map<String, Object> doc, String name, int fallbackValue) {
        final Object value = doc.get(name);
        return (value instanceof Number) ? ((Number) value).intValue() : fallbackValue;
    }
}
