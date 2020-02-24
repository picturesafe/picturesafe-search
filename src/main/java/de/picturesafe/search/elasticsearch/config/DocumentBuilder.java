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

package de.picturesafe.search.elasticsearch.config;

import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple builder for elasticsearch index documents.
 */
public class DocumentBuilder {

    private DocumentBuilder(long id) {
        doc.put(FieldConfiguration.FIELD_NAME_ID, id);
    }

    final Map<String, Object> doc = new HashMap<>();

    public static DocumentBuilder id(long id) {
        Validate.isTrue(id > 0, "Argument 'id' must be > 0!");
        return new DocumentBuilder(id);
    }

    public DocumentBuilder put(String fieldname, Object value) {
        doc.put(fieldname, value);
        return this;
    }

    public Map<String, Object> toDcoument() {
        return doc;
    }
}
