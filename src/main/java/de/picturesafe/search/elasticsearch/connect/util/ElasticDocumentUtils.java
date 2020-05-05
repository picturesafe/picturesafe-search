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

import de.picturesafe.search.elasticsearch.model.IdFormat;
import org.apache.commons.collections.MapUtils;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils.parseIso;

public class ElasticDocumentUtils {

    private ElasticDocumentUtils() {
    }

    public static long getId(Map<String, Object> doc, long fallbackValue) {
        final Long id = getId(doc, Long.class);
        return (id != null) ? id : fallbackValue;
    }

    public static <T> T getId(Map<String, Object> doc, Class<T> type) {
        final String val = getId(doc);
        return (val != null) ? IdFormat.DEFAULT.parse(val, type) : null;
    }

    public static String getId(Map<String, Object> doc) {
        return (String) doc.get("id");
    }

    public static Date getDate(Map<String, Object> doc, String name) {
        final String value = getString(doc, name);
        return (value != null) ? parseIso(value) : null;
    }

    public static String getString(Map<String, Object> doc, String name) {
        return MapUtils.getString(doc, name);
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getStringSet(Map<String, Object> doc, String name) {
        final Object value = doc.get(name);
        return (value instanceof Collection) ? new TreeSet<>(((Collection<String>) value)) : null;
    }

    public static long getLong(Map<String, Object> doc, String name, long fallbackValue) {
        return MapUtils.getLongValue(doc, name, fallbackValue);
    }

    public static int getInt(Map<String, Object> doc, String name, int fallbackValue) {
        return MapUtils.getIntValue(doc, name, fallbackValue);
    }

    public static Integer getInteger(Map<String, Object> doc, String name) {
        return MapUtils.getInteger(doc, name);
    }

    public static boolean getBoolean(Map<String, Object> doc, String name) {
        return MapUtils.getBooleanValue(doc, name);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getDocument(Map<String, Object> doc, String name) {
        final Object value = doc.get(name);
        return (value != null) ? (Map<String, Object>) value : null;
    }

    @SuppressWarnings("unchecked")
    public static Collection<Map<String, Object>> getDocuments(Map<String, Object> doc, String name) {
        final Object value = doc.get(name);
        return (value instanceof Collection) ? (Collection<Map<String, Object>>) value : null;
    }
}
