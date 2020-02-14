/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.error.AliasAlreadyExistsException;
import de.picturesafe.search.elasticsearch.connect.error.AliasCreateException;
import de.picturesafe.search.elasticsearch.connect.error.AliasHasMoreThanOneIndexException;
import de.picturesafe.search.elasticsearch.connect.error.IndexCreateException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.util.List;

public interface ElasticsearchAdmin {

    /**
     * Creates a new index.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @return                              Name of the new index
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
     * Creates a new alias.
     *
     * @param indexAlias    Name of the alias to create
     * @param indexName     Name of the index to be mapped to the alias
     */
    void createAlias(String indexAlias, String indexName) throws AliasCreateException, AliasAlreadyExistsException;

    /**
     * Removes an alias.
     *
     * @param indexAlias     {@link IndexPresetConfiguration}
     * @return               Name of the index which was mapped to the alias
     *
     * @throws AliasHasMoreThanOneIndexException    If alias has more than one indices
     */
    String removeAlias(String indexAlias) throws AliasHasMoreThanOneIndexException;

    /**
     * Adds one or more field configurations to the index mapping.
     *
     * @param indexPresetConfiguration      {@link IndexPresetConfiguration}
     * @param mappingConfiguration          {@link MappingConfiguration}
     * @param fieldConfigs                  Field configurations to be add to mapping
     */
    void updateMapping(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration, List<FieldConfiguration> fieldConfigs);

    /**
     * Deletes index with given name.
     *
     * @param indexName     Name of index to be deleted
     */
    void deleteIndex(String indexName);

    /**
     * Deletes indexes with the given alias name.
     *
     * @param indexAlias The alias name for which all indexes are to be deleted
     */
    void deleteIndexesOfAlias(String indexAlias);

    /**
     * Returns the index names according to the given <code>indexAlias</code>.
     *
     * @param indexAlias    The alias name
     * @return              Index names of given <code>indexAlias</code>
     */
    List<String> resolveIndexNames(String indexAlias);

    /**
     * Checks if an index with this alias name already exists.
     *
     * @param indexAlias The alias name
     * @return           True, if an index with this alias name already exists
     */
    boolean aliasOrIndexExists(String indexAlias);

    /**
     * Get index mapping as JSON string.
     *
     * @param indexName     The index name
     * @return              The index mapping as JSON string
     */
    String getMappingAsJson(String indexName);

    /**
     * <p>
     * Retrieves the cluster status and checks whether the status is at least the given (or a "better") status.
     * If an index name is given, the status for this index is queried.
     * <p>
     * The method checks (several times if necessary) whether the requested status is reached or exceeded.
     * As soon as the state of the cluster fulfills the request, the method returns <code>true</code>.
     * <p>
     * If the requested cluster state has not been reached even after the given timeout, <code>false</code> is returned.
     *
     * @param indexName             Index name
     * @param minStatus             the minimum status to be reached
     * @param timeoutInMsec         Timeout
     * @return                      True, if the status "green" was reached within the timeout
     */
    boolean waitForMinStatus(String indexName, ClusterHealthStatus minStatus, long timeoutInMsec);

    /**
     * Gets mapping configuration of given fieldname.
     *
     * @param mappingConfiguration  {@link MappingConfiguration}
     * @param fieldName             Fieldname
     * @return                      Mmapping configuration of given fieldname
     */
    FieldConfiguration fieldConfiguration(MappingConfiguration mappingConfiguration, String fieldName);

    /**
     * Gets the elasticsearch rest client.
     *
     * @return  The elasticsearch rest client
     */
    RestHighLevelClient getRestClient();

}
