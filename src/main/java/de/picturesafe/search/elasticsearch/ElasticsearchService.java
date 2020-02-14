/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.error.AliasAlreadyExistsException;
import de.picturesafe.search.elasticsearch.model.AccountContext;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.SuggestExpression;
import de.picturesafe.search.parameter.SearchParameter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service interface to interact with elasticsearch.
 */
@SuppressWarnings("unused")
public interface ElasticsearchService {

    /**
     * Gets Elasticsearch infos like client and server version.
     *
     * @see ElasticsearchInfo
     * @return  Elasticsearch infos
     */
    ElasticsearchInfo getElasticsearchInfo();

    /**
     * Creates a new index, fills up the index with data provided by a {@link DocumentProvider} and creates an alias for the index.
     * <p>
     * If the index exists and shall be rebuild, the new index is created in parallel to the existing one. After index creation has finished,
     * the alias will be switched and the old index will be deleted. All modifications to index data which is done while index rebuild is
     * processed will be stored temporarily and will be processed on the new index after rebuild has finished.
     * </p>
     *
     * @param indexAlias                Name of the alias
     * @param rebuildIfExists           TRUE if an existing index shall be rebuild, otherwise a {@link AliasAlreadyExistsException} will be thrown.
     * @param listener                  Listener for index initialization progress
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     *
     * @throws AliasAlreadyExistsException  If index exists and rebuildIfExists not true
     */
    void createAndInitializeIndex(String indexAlias, boolean rebuildIfExists, IndexInitializationListener listener,
                                  DataChangeProcessingMode dataChangeProcessingMode) throws AliasAlreadyExistsException;

    /**
     * Tests if an alias exists.
     *
     * @param indexAlias    Name of the alias
     * @return              TRUE if an alias with the given name exists
     */
    boolean aliasExists(String indexAlias);

    /**
     * Creates a new index.
     *
     * @param indexAlias    Name of the alias (used to generate the index name)
     * @return              Name of the new index
     */
    String createIndex(String indexAlias);

    /**
     * Creates a new index with alias.
     *
     * @param indexAlias    Name of the alias (used to generate the index name)
     * @return              Name of the new index
     */
    String createIndexWithAlias(String indexAlias);

    /**
     * Adds one or more field configurations to the index mapping.
     *
     * @param indexAlias    Name of the alias (used to generate the index name)
     * @param fieldConfigs  Field configurations to add
     */
    void addFieldConfiguration(String indexAlias, FieldConfiguration... fieldConfigs);

    /**
     * Deletes an index.
     *
     * @param indexName Name of the index to be deleted
     */
    void deleteIndex(String indexName);

    /**
     * Deletes an index with alias.
     *
     * @param indexAlias Index alias of the index to be deleted
     */
    void deleteIndexWithAlias(String indexAlias);

    /**
     * Resolves the names of the indexes mapped to an alias.
     *
     * @param indexAlias Name of the alias
     * @return Names of the indexes mapped by the alias
     */
    List<String> resolveIndexNames(String indexAlias);

    /**
     * Creates a new alias.
     *
     * @param indexAlias    Name of the alias to create
     * @param indexName     Name of the index to be mapped to the alias
     */
    void createAlias(String indexAlias, String indexName);

    /**
     * Removes an alias.
     *
     * @param indexAlias    Name of the alias
     * @return              Name of the index which was mapped to the alias
     */
    String removeAlias(String indexAlias);

    /**
     * Sets the version of the index.
     *
     * @param indexAlias    Name of the alias
     * @param indexVersion  Version of the index
     */
    void setIndexVersion(String indexAlias, int indexVersion);

    /**
     * Gets the version of the index.
     *
     * @param indexAlias    Name of the alias
     * @return              Version of the index
     */
    int getIndexVersion(String indexAlias);

    /**
     * Adds a document to the index. If a document with the same ID already exists it will be updated.
     * NOTE: key "id" must be present in document.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param document                  Document to be added
     */
    void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Map<String, Object> document);

    /**
     * Adds multiple documents to the index. If a document with the same ID already exists it will be updated.
     * NOTE: key "id" must be present in documents.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param documents                 Documents to be added
     */
    void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, List<Map<String, Object>> documents);

    /**
     * Removes a document from the index.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param id                        ID of the document to be removed
     */
    void removeFromIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, long id);

    /**
     * Removes multiple documents from the index.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param ids                       IDs of the documents to be removed
     */
    void removeFromIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Collection<Long> ids);

    /**
     * Searches for documents.
     *
     * @param indexAlias        Name of the alias of the index
     * @param accountContext    {@link AccountContext} of the current user
     * @param expression        Expression defining the search criteria
     * @param searchParameter   Parameters for the search execution
     * @return                  {@link SearchResult}
     */
    SearchResult search(String indexAlias, AccountContext accountContext, Expression expression, SearchParameter searchParameter);

    /**
     * Gets a document from the index.
     *
     * @param indexAlias    Name of the alias of the index
     * @param id            ID of the documents
     * @return              The document or <code>null</code> if the ID does not exist
     */
    Map<String, Object> getDocument(String indexAlias, long id);

    /**
     * Suggests text options for search-as-you-type functionality.
     *
     * @param indexAlias    Name of the alias of the index
     * @param expressions   Suggest expressions
     * @return              SuggestResult
     */
    SuggestResult suggest(String indexAlias, SuggestExpression... expressions);
}
