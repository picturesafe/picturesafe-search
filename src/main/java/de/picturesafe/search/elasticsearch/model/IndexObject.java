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

import org.apache.commons.lang3.Validate;

import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;

/**
 * Interface for objects that can be converted to/from elasticsearch index documents.
 *
 * NOTE: Implementations must have a default constructor.
 */
public interface IndexObject<T extends IndexObject<T>> {

    /**
     * Converts object to elasticsearch index document.
     * @return Elasticsearch index document
     */
    Map<String, Object> toDocument();

    /**
     * Converts elasticsearch index document to object.
     * @param document Elasticsearch index document
     * @return Object
     */
    T fromDocument(Map<String, Object> document);

    /**
     * Gets the class name from an elastcisearch index document.
     * @param document Elasticsearch index document
     * @return Class name
     */
    static String classNameFromDocument(Map<String, Object> document) {
        Validate.notEmpty(document, "Parameter 'document' may not be null or empty!");

        final String className = getString(document, "class");
        Validate.notNull(className, "Missing field 'class' in document!");
        return className;
    }
}
