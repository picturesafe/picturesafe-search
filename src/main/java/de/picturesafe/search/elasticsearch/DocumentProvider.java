/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch;

import java.util.List;
import java.util.Map;

/**
 * Service interface of a document provider
 */
public interface DocumentProvider {

    /**
     * Loads the documents for an index and provides them in chunks to the {@link DocumentHandler};
     *
     * @param indexAlias Name of the alias
     * @param handler {@link DocumentHandler} for processing the loaded document chunks
     */
    void loadDocuments(String indexAlias, DocumentHandler handler);

    /**
     * Handler interface for processing the loaded document chunks
     */
    interface DocumentHandler {
        void handleDocuments(DocumentChunk chunk);
    }

    /**
     * Chunk of loaded documents
     */
    class DocumentChunk {
        private final List<Map<String, Object>> documents;
        private final long documentsProcessed;
        private final long totalDocuments;

        /**
         * Constructor
         *
         * @param documents Documents
         * @param documentsProcessed Number of processed documents
         * @param totalDocuments Total number of documents
         */
        public DocumentChunk(List<Map<String, Object>> documents, long documentsProcessed, long totalDocuments) {
            this.documents = documents;
            this.documentsProcessed = documentsProcessed;
            this.totalDocuments = totalDocuments;
        }

        /**
         * Gets the documents.
         *
         * @return Documents
         */
        public List<Map<String, Object>> getDocuments() {
            return documents;
        }

        /**
         * Gets the number of processed documents.
         *
         * @return Number of processed documents
         */
        public long getDocumentsProcessed() {
            return documentsProcessed;
        }

        /**
         * Gets the total number of documents.
         *
         * @return Total number of documents
         */
        public long getTotalDocuments() {
            return totalDocuments;
        }
    }
}
