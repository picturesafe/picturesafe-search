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

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.model.IndexObject;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.SuggestExpression;
import de.picturesafe.search.parameter.AccountContext;
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
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param document                  Document to be added
     */
    void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Map<String, Object> document);

    /**
     * Adds an object to the index. If an object with the same ID already exists it will be updated.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param object                    Index object to be added
     */
    void addObjectToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object);

    /**
     * Adds an object to the index. If an object with the same ID already exists it will be updated.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param object                    Index object to be added
     * @param id                        ID to assign to the persisted object (useful if the object does not provide an ID itself)
     */
    void addObjectToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object, long id);

    /**
     * Adds multiple documents to the index. If a document with the same ID already exists it will be updated.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param documents                 Documents to be added
     */
    void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, List<Map<String, Object>> documents);

    /**
     * Adds multiple objects to the index. If an object with the same ID already exists it will be updated.
     *
     * @param indexAlias                Name of the alias of the index
     * @param dataChangeProcessingMode  {@link DataChangeProcessingMode}
     * @param objects                   Objects to be added
     */
    void addObjectsToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, List<IndexObject<?>> objects);

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
     * @param expression        Expression defining the search criteria
     * @param searchParameter   Parameters for the search execution
     * @return                  {@link SearchResult}
     */
    SearchResult search(String indexAlias, Expression expression, SearchParameter searchParameter);

    /**
     * Searches for documents in the context of an user account.
     *
     * @param indexAlias        Name of the alias of the index
     * @param accountContext    {@link AccountContext} of the current user
     * @param expression        Expression defining the search criteria
     * @param searchParameter   Parameters for the search execution
     * @return                  {@link SearchResult}
     */
    SearchResult search(String indexAlias, AccountContext<?> accountContext, Expression expression, SearchParameter searchParameter);

    /**
     * Gets a document from the index.
     *
     * @param indexAlias    Name of the alias of the index
     * @param id            ID of the document
     * @return              The document or <code>null</code> if the ID does not exist
     */
    Map<String, Object> getDocument(String indexAlias, long id);

    /**
     * Gets an object from the index.
     *
     * @param indexAlias    Name of the alias of the index
     * @param id            ID of the object
     * @param type          Type class of the object
     * @param <T>           Generic type of the object
     * @return              The index object or <code>null</code> if the ID does not exist
     */
    <T extends IndexObject<T>> T getObject(String indexAlias, long id, Class<T> type);

    /**
     * Suggests text options for search-as-you-type functionality.
     *
     * @param indexAlias    Name of the alias of the index
     * @param expressions   Suggest expressions
     * @return              SuggestResult
     */
    SuggestResult suggest(String indexAlias, SuggestExpression... expressions);
}
