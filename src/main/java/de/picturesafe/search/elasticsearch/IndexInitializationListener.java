/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Listener interface for index initialization progress
 */
public interface IndexInitializationListener {

    /**
     * Update the progress of an index initialization process.
     *
     * @param event {@link Event} of the index initialization process
     */
    void updateProgress(Event event);

    /**
     * Event of the index initialization progress
     */
    class Event {

        public enum Type {
            CREATE_INDEX,
            ADD_DOCUMENTS,
            SET_ALIAS,
            PROCESS_DELTA,
            DELETE_OLD_INDEX,
            END,
            ERROR
        }

        private final String indexAlias;
        private final Type type;
        private final long documentsProcessed;
        private final long totalDocuments;

        /**
         * Contructor
         *
         * @param indexAlias Name of the alias
         * @param type Type of event
         * @param documentsProcessed Number of processed documents
         * @param totalDocuments Total number of documents
         */
        public Event(String indexAlias, Type type, long documentsProcessed, long totalDocuments) {
            this.indexAlias = indexAlias;
            this.type = type;
            this.documentsProcessed = documentsProcessed;
            this.totalDocuments = totalDocuments;
        }

        /**
         * Gets the name of the alis.
         *
         * @return Name of the alias
         */
        public String getIndexAlias() {
            return indexAlias;
        }

        /**
         * Gets the type of the event.
         *
         * @return Type of the event
         */
        public Type getType() {
            return type;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Event)) {
                return false;
            }

            final Event that = (Event) o;
            return new EqualsBuilder()
                    .append(indexAlias, that.indexAlias)
                    .append(type, that.type)
                    .append(documentsProcessed, that.documentsProcessed)
                    .append(totalDocuments, that.totalDocuments)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(indexAlias)
                    .append(type)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                    .append("indexAlias", indexAlias) //--
                    .append("type", type) //--
                    .append("documentsProcessed", documentsProcessed) //--
                    .append("totalDocuments", totalDocuments) //--
                    .toString();
        }
    }
}
