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
