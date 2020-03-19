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

import java.util.Map;

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
}
