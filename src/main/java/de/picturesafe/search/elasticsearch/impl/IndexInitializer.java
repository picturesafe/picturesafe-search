/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.DocumentProvider;
import de.picturesafe.search.elasticsearch.IndexInitializationListener;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.util.logging.StopWatchPrettyPrint;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.List;

/**
 * Initializer for elasticsearch indexes. It will create the index, add documents, set the alias and delete an old index referred by that alias.
 */
class IndexInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexInitializer.class);

    private final InternalElasticsearchService elasticsearchService;
    private final DocumentProvider documentProvider;
    private final IndexRequestCache indexRequestCache;

    /**
     * Constructor
     *
     * @param elasticsearchService {@link InternalElasticsearchService}
     * @param documentProvider {@link DocumentProvider}
     * @param indexRequestCache {@link IndexRequestCache}
     */
    IndexInitializer(InternalElasticsearchService elasticsearchService, DocumentProvider documentProvider, IndexRequestCache indexRequestCache) {
        this.elasticsearchService = elasticsearchService;
        this.documentProvider = documentProvider;
        this.indexRequestCache = indexRequestCache;
    }

    /**
     * Initializes an index.
     *
     * @param indexAlias    Name of the alias
     * @param listener      Listener to track progress
     * @return              Name of the new index
     */
    String init(String indexAlias, IndexInitializationListener listener) {
        String indexName = null;
        long documentCount = 0L;

        try {
            final StopWatch sw = new StopWatch("Initialize index: " + indexAlias);
            sw.start("Create index");
            updateProgress(listener, new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.CREATE_INDEX, 0, 0));
            final boolean aliasExisted = elasticsearchService.aliasExists(indexAlias);
            final List<String> oldIndexNames = aliasExisted ? getOldIndexNames(indexAlias) : null;
            indexName = elasticsearchService.createIndex(indexAlias);
            if (!aliasExisted) {
                updateProgress(listener,
                        new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.SET_ALIAS, documentCount, documentCount));
                elasticsearchService.createAlias(indexAlias, indexName);
            }
            sw.stop();

            sw.start("Add documents");
            documentCount = addDocuments(indexAlias, indexName, listener);
            sw.stop();

            if (aliasExisted) {
                sw.start("Update alias");
                updateProgress(listener,
                        new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.SET_ALIAS, documentCount, documentCount));
                updateAlias(indexAlias, indexName);
                sw.stop();
            }

            final int deltaCount = indexRequestCache.dataSize(indexAlias);
            if (deltaCount > 0) {
                sw.start("Process delta");
                updateProgress(listener, new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.PROCESS_DELTA,
                        documentCount, documentCount + deltaCount));
                processDelta(indexAlias);
                documentCount += deltaCount;
                sw.stop();
            }

            if (CollectionUtils.isNotEmpty(oldIndexNames)) {
                sw.start("Delete old index");
                updateProgress(listener,
                        new IndexInitializationListener
                                .Event(indexAlias, IndexInitializationListener.Event.Type.DELETE_OLD_INDEX, documentCount, documentCount));
                for (String oldIndexName : oldIndexNames)
                elasticsearchService.deleteIndex(oldIndexName);
                sw.stop();
            }

            updateProgress(listener,
                    new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.END, documentCount, documentCount));
            LOGGER.debug("{}", new StopWatchPrettyPrint(sw));
        } catch (Exception e) {
            LOGGER.error("Failed to initialize index: alias = {}", indexAlias, e);
            updateProgress(listener, new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ERROR, 0, documentCount));
        }

        return indexName;
    }

    List<String> getOldIndexNames(String indexAlias) {
        return elasticsearchService.resolveIndexNames(indexAlias);
    }

    long addDocuments(String indexAlias, String indexName, IndexInitializationListener listener) {
        final MutableLong documentsProcessed = new MutableLong();
        final MutableLong totalDocuments = new MutableLong();
        indexRequestCache.start(indexAlias);
        try {
            final MappingConfiguration mappingConfiguration = elasticsearchService.getMappingConfiguration(indexAlias);
            documentProvider.loadDocuments(indexAlias, chunk -> {
                updateProgress(listener,
                        new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS,
                                chunk.getDocumentsProcessed(), chunk.getTotalDocuments()));
                elasticsearchService.addToIndex(mappingConfiguration, indexName, DataChangeProcessingMode.BACKGROUND, chunk.getDocuments());
                documentsProcessed.add(chunk.getDocumentsProcessed());
                totalDocuments.setValue(chunk.getTotalDocuments());
            });
        } catch (Exception e) {
            LOGGER.error("Failed to load documents for index: name = {},  alias = {}", indexName, indexAlias, e);
        }

        return documentsProcessed.longValue();
    }

    private void updateAlias(String indexAlias, String indexName) {
        final int indexVersion = elasticsearchService.getIndexVersion(indexAlias);
        elasticsearchService.removeAlias(indexAlias);
        elasticsearchService.createAlias(indexAlias, indexName);
        indexRequestCache.stop(indexAlias);

        if (indexVersion > -1) {
            elasticsearchService.setIndexVersion(indexAlias, indexVersion);
        }
    }

    private void processDelta(String indexAlias) {
        indexRequestCache.stop(indexAlias);
        indexRequestCache.replay(elasticsearchService, indexAlias);
        indexRequestCache.clear(indexAlias);
    }

    private void updateProgress(IndexInitializationListener listener, IndexInitializationListener.Event event) {
        if (listener != null) {
            try {
                listener.updateProgress(event);
            } catch (Exception e) {
                LOGGER.error("IndexInitializationListener threw exception: event = " + event, e);
            }
        }
    }
}
