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

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.impl.mapping.MappingFields;
import de.picturesafe.search.elasticsearch.impl.mapping.MappingResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MappingFieldConfigurationProvider implements FieldConfigurationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingFieldConfigurationProvider.class);
    private static final long DEFAULT_CACHE_TIMEOUT_MILLIS = 30_000;

    private final ElasticsearchAdmin elasticsearchAdmin;
    private final long cacheTimeoutMillis;

    private final Map<String, CachedFields> cache = new HashMap<>();
    private final Lock cacheLock = new ReentrantLock();

    public MappingFieldConfigurationProvider(ElasticsearchAdmin elasticsearchAdmin) {
        this(elasticsearchAdmin, DEFAULT_CACHE_TIMEOUT_MILLIS);
    }

    public MappingFieldConfigurationProvider(ElasticsearchAdmin elasticsearchAdmin, long cacheTimeoutMillis) {
        this.elasticsearchAdmin = elasticsearchAdmin;
        this.cacheTimeoutMillis = cacheTimeoutMillis;
    }

    @Override
    public List<? extends FieldConfiguration> getFieldConfigurations(String indexAlias) {
        return getMappingFields(indexAlias).getFieldConfigurations();
    }

    @Override
    public List<Locale> getSupportedLocales(String indexAlias) {
        return getMappingFields(indexAlias).getSupportedLocales();
    }

    private MappingFields getMappingFields(String indexAlias) {
        cacheLock.lock();
        try {
            final long now = System.currentTimeMillis();
            CachedFields cachedFields = cache.computeIfAbsent(indexAlias, alias -> new CachedFields(loadMappingFields(alias), now));
            if (cacheTimeoutMillis > 0 && now - cachedFields.cacheTime >= cacheTimeoutMillis) {
                LOGGER.debug("Cache timed out for index mapping: alias={}, now={}, cacheTime={}", indexAlias, now, cachedFields.cacheTime);
                cachedFields = cache.compute(indexAlias, (alias, s) -> new CachedFields(loadMappingFields(alias), now));
            }
            return cachedFields.fields;
        } finally {
            cacheLock.unlock();
        }
    }

    private MappingFields loadMappingFields(String indexAlias) {
        final List<String> indexNames = elasticsearchAdmin.resolveIndexNames(indexAlias);
        if (indexNames.size() > 1) {
            throw new RuntimeException("Alias '" + indexAlias + "' refers to multiple indexes, cannot resolve single mapping!");
        }
        final String indexName = indexNames.get(0);
        return MappingResolver.resolveFields(elasticsearchAdmin.getMapping(indexName), indexName);
    }

    private static class CachedFields {
        final MappingFields fields;
        final long cacheTime;

        CachedFields(MappingFields fields, long cacheTime) {
            this.fields = fields;
            this.cacheTime = cacheTime;
        }
    }
}
