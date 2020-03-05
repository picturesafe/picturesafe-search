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

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.error.AliasAlreadyExistsException;
import de.picturesafe.search.elasticsearch.connect.error.AliasCreateException;
import de.picturesafe.search.elasticsearch.connect.error.AliasHasMoreThanOneIndexException;
import de.picturesafe.search.elasticsearch.connect.error.IndexCreateException;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.expression.SuggestExpression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public interface Elasticsearch {

    String INDEX_VERSION = "index_version";

    /**
     * Adds a document to the index. If a document with the same ID already exists it will be updated.
     * NOTE: key "id" must be present in document.
     *
     * @param document                  Document to be added
     * @param mappingConfiguration      {@link MappingConfiguration}
     * @param indexAlias                The index alias
     * @param applyIndexRefresh         Should the search index be forced to be updated immediately?
     *                                  Be careful and use false as default.
     */
    void addToIndex(Map<String, Object> document, MappingConfiguration mappingConfiguration, String indexAlias, boolean applyIndexRefresh);

    /**
     * Adds multiple documents to the index. If a document with the same ID already exists it will be updated.
     * NOTE: key "id" must be present in documents.
     *
     * @param documents                 Documents to be added
     * @param mappingConfiguration      {@link MappingConfiguration}
     * @param indexAlias                The index alias
     * @param applyIndexRefresh         Should the search index be forced to be updated immediately?
     *                                  Be careful and use false as default.
     * @param execeptionOnFailure       Throw Exception if update of at least one document fails
     * @return                          Status of indexing per document (id, true|false)
     */
    Map<String, Boolean> addToIndex(List<Map<String, Object>> documents,
                                  MappingConfiguration mappingConfiguration,
                                  String indexAlias,
                                  boolean applyIndexRefresh,
                                  boolean execeptionOnFailure);

    /**
     * Removes a document from the index.
     *
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param applyIndexRefresh             Should the search index be forced to be updated immediately?
     *                                      Be careful and use false as default.
     * @param id                            ID of the document to be removed
     */
    void removeFromIndex(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration, boolean applyIndexRefresh, long id);

    /**
     * Removes multiple documents from the index.
     *
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param applyIndexRefresh             Should the search index be forced to be updated immediately?
     *                                      Be careful and use false as default.
     * @param ids                           IDs of the documents to be removed
     */
    void removeFromIndex(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration,
                         boolean applyIndexRefresh, Collection<Long> ids);

    /**
     * Checks if elasticsearch service is available.
     *
     * @return true if elasticsearch service is available
     */
    boolean isServiceAvailable();

    /**
     * Refresh one or more indices.
     *
     * @param indexAlias     The alias name of the index to be refreshed
     */
    void refresh(String indexAlias);

    /**
     * Creates a new index.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @return                              Name of the new index
     *
     * @throws IndexCreateException         Creating index failed
     */
    String createIndex(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) throws IndexCreateException;

    /**
     * Creates a new index with alias.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @return                              Name of the (new) index
     * @throws IndexCreateException         Creating index failed
     * @throws AliasCreateException         The alias could not be created
     * @throws AliasAlreadyExistsException  The alias already exists
     */
    String createIndexWithAlias(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration)
            throws IndexCreateException, AliasCreateException, AliasAlreadyExistsException;

    /**
     * Deletes index with given name.
     *
     * @param indexName     Name of index to be deleted
     */
    void deleteIndex(String indexName);

    /**
     * Creates a new alias.
     *
     * @param indexAlias    Name of the alias to create
     * @param indexName     Name of the index to be mapped to the alias
     *
     * @throws AliasCreateException         The alias could not be created
     * @throws AliasAlreadyExistsException  The alias already exists
     */
    void createAlias(String indexAlias, String indexName) throws AliasCreateException, AliasAlreadyExistsException;

    /**
     * Removes an alias.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @return                              Name of the index which was mapped to the alias
     *
     * @throws AliasHasMoreThanOneIndexException    If alias has more than one indices
     */
    String removeAlias(IndexPresetConfiguration indexPresetConfiguration) throws AliasHasMoreThanOneIndexException;

    /**
     * Tests if an alias exists.
     *
     * @param indexAlias    Name of the alias
     * @return              TRUE if an alias with the given name exists
     */
    boolean aliasExists(String indexAlias);

    /**
     * Resolves the names of the indexes mapped to an alias.
     *
     * @param indexAlias    Name of the alias
     * @return              Names of the indexes mapped by the alias
     */
    List<String> resolveIndexNames(String indexAlias);

    /**
     * Gets the version of the index.
     *
     * @param indexAlias    Name of the alias
     * @return              Version of the index
     */
    int getIndexVersion(String indexAlias);

    /**
     * Sets the version of the index.
     *
     * @param indexAlias            Name of the alias
     * @param indexVersion          Version of the index
     * @param mappingConfiguration  {@link MappingConfiguration}
     */
    void setIndexVersion(String indexAlias, int indexVersion, MappingConfiguration mappingConfiguration);

    /**
     * Adds one or more field configurations to the index mapping.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @param fieldConfigs                  Field configurations to be add to mapping
     */
    void updateMapping(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration, List<FieldConfiguration> fieldConfigs);

    /**
     * Gets Elasticsearch infos like client and server version.
     *
     * @see ElasticsearchInfo
     * @return  Elasticsearch infos
     */
    ElasticsearchInfo getElasticsearchInfo();

    /**
     * Searches for documents.
     *
     * @param queryDto                      {@link QueryDto}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @return                              {@link ElasticsearchResult}
     */
    ElasticsearchResult search(QueryDto queryDto, MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration);

    /**
     * Gets a document from the index.
     *
     * @param indexAlias      Name of the alias of the index
     * @param id              ID of the documents
     * @return                The document or <code>null</code> if the ID does not exist
     */
    Map<String, Object> getDocument(String indexAlias, long id);

    /**
     * Suggests text options for search-as-you-type functionality.
     *
     * @param indexAlias    Name of the alias of the index
     * @param expressions   Suggest expressions
     * @return              Suggest result per field
     */
    Map<String, List<String>> suggest(String indexAlias, SuggestExpression... expressions);
}
