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

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representation of a cached index request.
 */
class IndexRequest {

    enum Type {ADD, REMOVE}

    final Type type;
    final List<Map<String, Object>> docs;
    final Collection<Long> ids;

    private IndexRequest(Type type, List<Map<String, Object>> docs, Collection<Long> ids) {
        this.type = type;
        this.docs = docs;
        this.ids = ids;
    }

    /**
     * Contructs an add request.
     *
     * @param docs Documents to be added
     * @return IndexRequest
     */
    static IndexRequest add(List<Map<String, Object>> docs) {
        return new IndexRequest(Type.ADD, docs, null);
    }

    /**
     * Contructs an add request.
     *
     * @param docs Documents to be added
     * @return IndexRequest
     */
    @SafeVarargs
    static IndexRequest add(Map<String, Object>... docs) {
        return new IndexRequest(Type.ADD, Arrays.asList(docs), null);
    }

    /**
     * Contructs an add request.
     *
     * @param doc Document to be added
     * @return IndexRequest
     */
    static IndexRequest add(Map<String, Object> doc) {
        return new IndexRequest(Type.ADD, Collections.singletonList(doc), null);
    }

    /**
     * Constructs a remove request.
     *
     * @param ids IDs to be removed
     * @return IndexRequest
     */
    static IndexRequest remove(Collection<Long> ids) {
        return new IndexRequest(Type.REMOVE, null, ids);
    }

    /**
     * Constructs a remove request.
     *
     * @param ids IDs to be removed
     * @return IndexRequest
     */
    static IndexRequest remove(Long... ids) {
        return new IndexRequest(Type.REMOVE, null, Arrays.asList(ids));
    }

    /**
     * Constructs a remove request.
     *
     * @param id ID to be removed
     * @return IndexRequest
     */
    static IndexRequest remove(Long id) {
        return new IndexRequest(Type.REMOVE, null, Collections.singletonList(id));
    }

    /**
     * Gets the size of the data (documents or IDs).
     *
     * @return Size of data
     */
    int dataSize() {
        return (type == Type.ADD) ? docs.size() : ids.size();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("type", type) //--
                .append("docs", docs) //--
                .append("ids", ids) //--
                .toString();
    }
}
