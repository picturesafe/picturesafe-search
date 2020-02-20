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

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache for index requests which can be replayed later.
 */
class IndexRequestCache {

    private final Map<String, List<IndexRequest>> cache = new HashMap<>();

    /**
     * Starts accepting index requests being put to the cache.
     *
     * @param indexAlias Name of the alias
     */
    synchronized void start(String indexAlias) {
        if (!isStarted(indexAlias)) {
            cache.put(indexAlias, new ArrayList<>());
        }
    }

    /**
     * Checks if the cache has been started.
     *
     * @param indexAlias Name of the alias
     * @return TRUE if the cache has been started
     */
    synchronized boolean isStarted(String indexAlias) {
        final List<IndexRequest> requests = cache.get(indexAlias);
        return requests != null && !(requests instanceof StoppedList);
    }

    /**
     * Stops accepting index requests being put to the cache.
     *
     * @param indexAlias Name of the alias
     */
    synchronized void stop(String indexAlias) {
        cache.put(indexAlias, StoppedList.of(cache.get(indexAlias)));
    }

    /**
     * Put index request to the cache.
     *
     * @param indexAlias    Name of the alias
     * @param request       {@link IndexRequest}
     */
    synchronized void put(String indexAlias, IndexRequest request) {
        final List<IndexRequest> requests = cache.get(indexAlias);
        if (requests != null) {
            requests.add(request);
        }
    }

    /**
     * Replays the cached index requests. For each request the relating method of {@link ElasticsearchService} gets called.
     *
     * @param elasticsearchService  {@link ElasticsearchService}
     * @param indexAlias            Name of teh alias
     */
    synchronized void replay(ElasticsearchService elasticsearchService, String indexAlias) {
        final List<IndexRequest> requests = cache.get(indexAlias);
        if (requests instanceof StoppedList) {
            requests.forEach(req -> {
                switch (req.type) {
                    case ADD:
                        elasticsearchService.addToIndex(indexAlias, DataChangeProcessingMode.BACKGROUND, req.docs);
                        break;
                    case REMOVE:
                        elasticsearchService.removeFromIndex(indexAlias, DataChangeProcessingMode.BACKGROUND, req.ids);
                        break;
                    default:
                        throw new RuntimeException("Unsupported request type: " + req.type);
                }
            });
        } else if (requests != null) {
            throw new IllegalStateException("Cache should be stopped before replay: " + indexAlias);
        }
    }

    /**
     * Clears the cache for the given alias. All index requests of this alias will be removed.
     *
     * @param indexAlias Name of the alias
     */
    synchronized void clear(String indexAlias) {
        cache.remove(indexAlias);
    }

    /**
     * Gets the size of the cache for the given alias.
     *
     * @param indexAlias Name of the alias
     * @return Size of the cache (number of index requests)
     */
    synchronized int size(String indexAlias) {
        final List<IndexRequest> requests = cache.get(indexAlias);
        return (requests != null) ? requests.size() : 0;
    }

    /**
     * Gets the data size of the cache for the given alias.
     *
     * @param indexAlias Name of the alias
     * @return Data size (number of documents or IDs)
     */
    synchronized int dataSize(String indexAlias) {
        final List<IndexRequest> requests = cache.get(indexAlias);
        final MutableInt size = new MutableInt();
        if (requests != null) {
            requests.forEach(req -> size.add(req.dataSize()));
        }
        return size.intValue();
    }

    private static class StoppedList<T> extends ArrayList<T> {

        public static <T> List<T> of(List<? extends T> list) {
            return new StoppedList<>(list);
        }

        private StoppedList(List<? extends T> list) {
            super(list);
        }

        @Override
        public boolean add(T element) {
            return false;
        }
    }
}
