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

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.timezone.TimeZoneAware;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple builder for elasticsearch index documents.
 */
public class DocumentBuilder implements TimeZoneAware  {

    final Map<String, Object> doc = new HashMap<>();
    final String timeZone;

    private DocumentBuilder() {
        timeZone = System.getProperty(TIME_ZONE_PROPERTY_KEY, DEFAULT_TIME_ZONE);
    }

    private DocumentBuilder(String id) {
        this();
        doc.put(FieldConfiguration.FIELD_NAME_ID, id);
    }

    public static DocumentBuilder id(Object id) {
        return id(id, IdFormat.DEFAULT);
    }

    public static DocumentBuilder id(Object id, IdFormat idFormat) {
        Validate.notNull(id, "Parameter 'id' may not be null!");
        Validate.notNull(id, "Parameter 'idFormat' may not be null!");
        return new DocumentBuilder(idFormat.format(id));
    }

    public static DocumentBuilder withoutId() {
        return new DocumentBuilder();
    }

    public DocumentBuilder put(String fieldname, IndexObject<?> value) {
        return put(fieldname, (value != null) ? value.toDocument() : null);
    }

    public DocumentBuilder put(String fieldname, Collection<? extends IndexObject<?>> values) {
        return put(fieldname, (values != null) ? values.stream().map(IndexObject::toDocument).collect(Collectors.toList()) : null);
    }

    public DocumentBuilder put(String fieldname, Date value) {
        if (value != null) {
            doc.put(fieldname, ElasticDateUtils.formatIso(value, timeZone));
        }
        return this;
    }

    public DocumentBuilder put(String fieldname, Object value) {
        if (value != null) {
            doc.put(fieldname, value);
        }
        return this;
    }

    public Map<String, Object> build() {
        return doc;
    }
}
