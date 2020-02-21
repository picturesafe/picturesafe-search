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

package de.picturesafe.search.elasticsearch.connect.impl;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexSettingsObject;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.connect.error.AliasAlreadyExistsException;
import de.picturesafe.search.elasticsearch.connect.error.AliasCreateException;
import de.picturesafe.search.elasticsearch.connect.error.AliasHasMoreThanOneIndexException;
import de.picturesafe.search.elasticsearch.connect.error.ElasticsearchException;
import de.picturesafe.search.elasticsearch.connect.error.IndexCreateException;
import de.picturesafe.search.elasticsearch.connect.mapping.MappingBuilder;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.elasticsearch.connect.util.logging.XcontentToString;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Component
public class ElasticsearchAdminImpl implements ElasticsearchAdmin, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchAdminImpl.class);

    protected static final String CHAR_FILTER_UMLAUT_MAPPING = "umlaut_mapping";
    protected static final String FILTER_WORD_DELIMITER = "filter_word_delimiter";

    protected RestClientConfiguration restClientConfiguration;
    protected RestHighLevelClient restClient;

    @Autowired
    public ElasticsearchAdminImpl(RestClientConfiguration restClientConfiguration) {
        this.restClientConfiguration = restClientConfiguration;
    }

    public void afterPropertiesSet() {
        this.restClient = restClientConfiguration.getClient();
    }

    @Override
    public String createIndex(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration)
            throws IndexCreateException {
        Validate.notNull(indexPresetConfiguration, "Parameter 'indexPresetConfiguration' may not be null!");
        Validate.notNull(mappingConfiguration, "Parameter 'mappingConfiguration' may not be null!");

        final String newIndexName = indexPresetConfiguration.createNewIndexName();
        try {
            LOG.info("Creating elasticsearch index '{}' with configuration: {}", newIndexName, indexPresetConfiguration);

            final CreateIndexRequest request = new CreateIndexRequest(newIndexName);
            final XContentBuilder indexSettings = createIndexSettings(indexPresetConfiguration);
            if (indexSettings != null) {
                request.settings(indexSettings);
            }
            request.mapping(new MappingBuilder(mappingConfiguration.getLanguageSortConfigurations()).build(mappingConfiguration));

            final CreateIndexResponse response = restClient.indices().create(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                throw new RuntimeException("Elasticsearch did not acknowledge index create request: " + response);
            }

            if (!indexExists(newIndexName)) {
                throw new RuntimeException("New index was created without any error but still does not exist: " + newIndexName);
            }
        } catch (Exception e) {
            final String message = "Failed to create or update elasticsearch index '" + newIndexName + "'.";
            LOG.error(message, e);

            try {
                deleteIndex(newIndexName);
                throw new IndexCreateException(
                        message + " The index should be deleted automatically. Please check the elasticsearch server logs before trying to create a new index.",
                        e, indexPresetConfiguration, mappingConfiguration);
            } catch (RuntimeException anyDeleteFailedException) {
                LOG.error("Deletion of elasticsearch index failed. Please also look for following errors.", anyDeleteFailedException);
                throw new IndexCreateException(
                        message + " You have to delete the index manually before creating a new index. In most cases the index metadata structure is corrupt. "
                                + "An automatic deletion of the corrupt index failed. (Please see previous log messages for details.)",
                        e, indexPresetConfiguration, mappingConfiguration);
            }
        }
        return newIndexName;
    }

    @Override
    public String createIndexWithAlias(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration)
            throws IndexCreateException, AliasCreateException, AliasAlreadyExistsException {
        Validate.notNull(indexPresetConfiguration, "Parameter 'indexPresetConfiguration' may not be null!");
        Validate.notNull(mappingConfiguration, "Parameter 'mappingConfiguration' may not be null!");

        final String indexName = createIndex(indexPresetConfiguration, mappingConfiguration);
        try {
            createAlias(indexPresetConfiguration.getIndexAlias(), indexName);
        } catch (AliasAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            try {
                deleteIndex(indexName);
                throw new IndexCreateException(
                        "Failed to create or Alias for the index '" + indexName + "'. The index should be "
                                + "deleted automatically. Please check the elasticsearch server logs before trying to create "
                                + "a new index.", e, indexPresetConfiguration, mappingConfiguration);
            } catch (RuntimeException anyDeleteFailedException) {
                LOG.error("Deletion of elasticsearch index failed. Please also look for following errors.", anyDeleteFailedException);
                throw new IndexCreateException(
                        "Failed to create or update elasticsearch index '" + indexName + "'. You have to delete the "
                                + "index manually before creating a new index. In most cases the index metadata structure "
                                + "is corrupt. An automatic deletion of the corrupt index failed. "
                                + "(Please see previous log messages for details.)", e, indexPresetConfiguration, mappingConfiguration);
            }
        }

        return indexName;
    }

    @Override
    public void createAlias(String indexAlias, String indexName)
            throws AliasCreateException, AliasAlreadyExistsException {
        Validate.notEmpty(indexAlias, "The argument 'indexAlias' is empty.");
        Validate.notEmpty(indexName, "The argument 'indexName' is empty.");

        try {
            final boolean existsAlias = restClient.indices().existsAlias(new GetAliasesRequest(indexAlias), RequestOptions.DEFAULT);
            if (!existsAlias) {
                final IndicesAliasesRequest request = new IndicesAliasesRequest();
                final IndicesAliasesRequest.AliasActions aliasAction =
                        new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD).index(indexName).alias(indexAlias);
                request.addAliasAction(aliasAction);
                final AcknowledgedResponse response = restClient.indices().updateAliases(request, RequestOptions.DEFAULT);
                if (!response.isAcknowledged()) {
                    throw new AliasCreateException("Elasticsearch did not acknowledge add alias request: " + response);
                }
                if (!aliasExists(indexAlias)) {
                    throw new AliasCreateException("New alias was created without any error but still does not exist: " + indexAlias);
                }
            } else {
                throw new AliasAlreadyExistsException("Elasticsearch alias already exists: " + indexAlias);
            }
        } catch (ElasticsearchStatusException | IOException e) {
            throw new AliasCreateException(indexAlias, indexName, e);
        }
    }

    @Override
    public String removeAlias(String indexAlias) throws AliasHasMoreThanOneIndexException {
        Validate.notEmpty(indexAlias, "The argument 'indexAlias' is empty.");

        final GetAliasesRequest getAliasesRequest = new GetAliasesRequest(indexAlias);
        final GetAliasesResponse getAliasesResponse;
        try {
            getAliasesResponse = restClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load alias with name '" + indexAlias + "'", ioe);
        }

        final List<String> indexesWithAlias = new ArrayList<>();
        getAliasesResponse.getAliases().forEach((indexName, aliasMetaData) -> {
            aliasMetaData.forEach(aliasMeta -> {
                if (indexAlias.equals(aliasMeta.alias())) {
                    indexesWithAlias.add(indexName);
                }
            });
        });
        if (CollectionUtils.isEmpty(indexesWithAlias)) {
            LOG.info("The alias '" + indexAlias + "' does not have any indexes");
            return null;
        } else if (indexesWithAlias.size() > 1) {
            throw new AliasHasMoreThanOneIndexException("The alias is only allowed to have one index, but has " + indexesWithAlias.size() + " indices");
        }

        final String[] indexArray = indexesWithAlias.toArray(new String[0]);
        final IndicesAliasesRequest indicesAliasesRequest = new IndicesAliasesRequest();
        final IndicesAliasesRequest.AliasActions aliasAction =
                new IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE).indices(indexArray).alias(indexAlias);
        indicesAliasesRequest.addAliasAction(aliasAction);
        try {
            final AcknowledgedResponse response = restClient.indices().updateAliases(indicesAliasesRequest, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                throw new AliasCreateException("Elasticsearch did not acknowledge remove alias indicesAliasesRequest: " + response);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove alias '" + indexAlias + "'", e);
        }
        return indexesWithAlias.get(0);
    }

    @Override
    public void updateMapping(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration,
                              List<FieldConfiguration> fieldConfigs) {
        Validate.notNull(indexPresetConfiguration, "Parameter 'indexPresetConfiguration' may not be null!");
        Validate.notNull(mappingConfiguration, "Parameter 'mappingConfiguration' may not be null!");
        Validate.notEmpty(fieldConfigs, "Parameter 'fieldConfigs' may not be null or empty!");

        try {
            final PutMappingRequest request = new PutMappingRequest(indexPresetConfiguration.getIndexAlias())
                    .source(new MappingBuilder(mappingConfiguration.getLanguageSortConfigurations()).buildUpdate(fieldConfigs));

            final AcknowledgedResponse response = restClient.indices().putMapping(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                throw new RuntimeException("Elasticsearch did not acknowledge put mapping request: " + response);
            }
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to add field configurations to index: " + indexPresetConfiguration.getIndexAlias(), e);
        }
    }

    @Override
    public void deleteIndex(String indexName) {
        try {
            restClient.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOG.error("Failed to delete the index '" + indexName + "'", e);
        }
        LOG.info("Deleted index '" + indexName + "'.");
    }

    @Override
    public void deleteIndexesOfAlias(String indexAlias) {
        Validate.notEmpty(indexAlias, "The argument 'indexAlias' must not be null or empty!");

        final List<String> indexNames = resolveIndexNames(indexAlias);
        if (indexNames.isEmpty()) {
            LOG.info("There are not any elasticsearch indices with the alias: " + indexAlias);
        } else {
            for (String indexName : indexNames) {
                deleteIndex(indexName);
            }
        }
    }

    @Override
    public List<String> resolveIndexNames(String alias) {
        Validate.notEmpty(alias, "The argument 'indexAlias' is null or empty.");

        final GetAliasesRequest getAliasesRequest = new GetAliasesRequest(alias);
        final GetAliasesResponse getAliasesResponse;
        try {
            getAliasesResponse = restClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load alias with name '" + alias + "'!", ioe);
        }
        final List<String> indexesWithAlias = new ArrayList<>();
        getAliasesResponse.getAliases().forEach((indexName, aliasMetaData) -> {
            aliasMetaData.forEach(aliasMeta -> {
                if (alias.equals(aliasMeta.alias())) {
                    indexesWithAlias.add(indexName);
                }
            });
        });
        return indexesWithAlias;
    }

    @Override
    public boolean aliasOrIndexExists(String aliasOrIndexName) {
        Validate.notEmpty(aliasOrIndexName, "The argument 'aliasOrIndexName' is null or empty.");
        return aliasExists(aliasOrIndexName) || indexExists(aliasOrIndexName);
    }

    @Override
    public String getMappingAsJson(String indexName) {
        final GetMappingsRequest request = new GetMappingsRequest();
        request.indices(indexName);
        final GetMappingsResponse getMappingResponse;
        try {
            getMappingResponse = restClient.indices().getMapping(request, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load mapping for index name '" + indexName + "'!", ioe);
        }
        final Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
        final MappingMetaData typeMapping = allMappings.get(indexName);
        return typeMapping.source().toString();
    }

    @Override
    public boolean waitForMinStatus(String indexName, ClusterHealthStatus minStatus, long timeoutInMsec) {
        Assert.isTrue(timeoutInMsec > 0, "timeout must be > 0");
        Assert.notNull(minStatus, "minStatus must be != null");

        String clusterName = null;
        ClusterHealthRequest request;
        ClusterHealthResponse response;
        try {
            request = new ClusterHealthRequest().local(true);
            response = restClient.cluster().health(request, RequestOptions.DEFAULT);

            clusterName = response.getClusterName();
        } catch (Exception e) {
            LOG.warn("Unable to retrieve cluster name.", e);
        }

        if (indexName == null) {
            LOG.info(new MessageFormat("Waiting at most timeout=\"{0}\" for a minimal cluster status of \"{1}\" for cluster named \"{2}\".", Locale.ROOT)
                    .format(new Object[]{timeoutInMsec, minStatus, clusterName}));
        } else {
            LOG.info(new MessageFormat("Waiting at most timeout=\"{0}\" for a minimal cluster status of \"{1}\" for cluster named \"{2}\" "
                    + "and indexName \"{3}\"", Locale.ROOT).format(new Object[]{timeoutInMsec, minStatus, clusterName, indexName}));
        }

        // Calculate increment ( 1/10 of the timeout, minimum 10 msec, maximum 1 sec)
        final long increment = Math.min(Math.max(timeoutInMsec / 10, 10), 1000);

        // calculate end time
        final long endTime = System.currentTimeMillis() + timeoutInMsec;

        try {
            // The loop is executed until the end time is reached
            while (endTime > System.currentTimeMillis()) {

                request = new ClusterHealthRequest().local(true).timeout(TimeValue.timeValueMillis(timeoutInMsec));
                if (indexName != null) {
                    request.indices(indexName);
                }

                response = restClient.cluster().health(request, RequestOptions.DEFAULT);
                final ClusterHealthStatus status = response.getStatus();
                if (status.value() <= minStatus.value()) {
                    LOG.debug("Cluster health status is " + status.name() + ".");
                    return true;
                }
                LOG.debug("Cluster health status is " + status.name() + ", still waiting for status " + minStatus.name() + ".");

                Thread.sleep(increment);
            }
        } catch (Exception e) {
            LOG.warn("Exception occured while waiting for cluster status " + minStatus.name() + ", perhaps the cluster is not reachable?", e);
        }
        LOG.warn("Minimal cluster status " + minStatus.name() + " not reached after more than " + timeoutInMsec + " msec, giving up!");
        return false;
    }

    @Override
    public FieldConfiguration fieldConfiguration(MappingConfiguration mappingConfiguration, String fieldName) {
        return FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
    }

    @Override
    public RestHighLevelClient getRestClient() {
        return restClient;
    }

    protected XContentBuilder createIndexSettings(IndexPresetConfiguration indexPresetConfiguration) {
        final XContentBuilder result;
        try {
            result = jsonBuilder();
            result.startObject();

            result.field("index.max_result_window", indexPresetConfiguration.getMaxResultWindow());
            result.field("index.number_of_replicas", indexPresetConfiguration.getNumberOfReplicas());
            result.field("index.number_of_shards", indexPresetConfiguration.getNumberOfShards());

            if (indexPresetConfiguration.getFieldsLimit() != null) {
                result.field("index.mapping.total_fields.limit", indexPresetConfiguration.getFieldsLimit());
            }
            if (indexPresetConfiguration.isUseCompression()) {
                result.field("index.codec", "best_compression");
            }

            final boolean hasCharMappings = indexPresetConfiguration.getCharMappings() != null;
            final boolean hasTokenizers = CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomTokenizers());
            final boolean hasAnalyzers = CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomAnalyzers());

            if (hasCharMappings || hasTokenizers || hasAnalyzers) {
                // start analysis configuration
                result.startObject("analysis");

                if (hasCharMappings) {
                    addFiltersToIndexSettings(result, indexPresetConfiguration);
                }
                if (hasTokenizers) {
                    addTokenizersToIndexSettings(result, indexPresetConfiguration);
                }
                if (hasCharMappings || hasAnalyzers) {
                    addAnalyzersToIndexSettings(result, indexPresetConfiguration);
                }

                // end analysis configuration object
                result.endObject();
            }

            result.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.debug("Index settings:\n{}", new XcontentToString(result));
        return result;
    }

    protected void addFiltersToIndexSettings(XContentBuilder result, IndexPresetConfiguration indexPresetConfiguration) {

        try {
            final Map<String, String> charMappings = indexPresetConfiguration.getCharMappings();
            if (charMappings != null) {
                final String[] mappings = new String[charMappings.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : charMappings.entrySet()) {
                    mappings[i++] = entry.getKey() + "=>" + entry.getValue();
                }

                // create char_filter
                result.startObject("char_filter");
                result.startObject(CHAR_FILTER_UMLAUT_MAPPING);
                result.field("type", "mapping");
                result.field("mappings", mappings);
                result.endObject();
                result.endObject();

                // create filter
                result.startObject("filter");
                result.startObject(FILTER_WORD_DELIMITER);
                result.field("type", "word_delimiter");
                result.field("split_on_numerics", false);
                result.field("split_on_case_change", false);
                result.endObject();
                result.endObject();
            }
        } catch (Exception e) {
            throw new RuntimeException("Adding filters to index settings failed!", e);
        }
    }

    protected void addTokenizersToIndexSettings(XContentBuilder result, IndexPresetConfiguration indexPresetConfiguration) {
        try {
            result.startObject("tokenizer");
            for (final IndexSettingsObject settings : indexPresetConfiguration.getCustomTokenizers()) {
                result.rawField(settings.name(), IOUtils.toInputStream(settings.json(), StandardCharsets.UTF_8), XContentType.JSON);
            }
            result.endObject();
        } catch (Exception e) {
            throw new RuntimeException("Adding tokenizers to index settings failed!", e);
        }
    }

    protected void addAnalyzersToIndexSettings(XContentBuilder result, IndexPresetConfiguration indexPresetConfiguration) {
        try {
            result.startObject("analyzer");

            // configure analyzer "default"
            if (indexPresetConfiguration.getCharMappings() != null) {
                // register customer char and token filter
                final String[] charFilters = {CHAR_FILTER_UMLAUT_MAPPING};
                final String[] filters = {FILTER_WORD_DELIMITER, "lowercase", "trim"};
                result.startObject("default");
                result.field("char_filter", charFilters);
                result.field("tokenizer", "standard");
                result.field("filter", filters);
                result.endObject();
            }

            // custom analyzers
            if (CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomAnalyzers())) {
                for (final IndexSettingsObject settings : indexPresetConfiguration.getCustomAnalyzers()) {
                    result.rawField(settings.name(), IOUtils.toInputStream(settings.json(), StandardCharsets.UTF_8), XContentType.JSON);
                }
            }

            result.endObject();
        } catch (Exception e) {
            throw new RuntimeException("Adding analyzers to index settings failed!", e);
        }
    }

    protected boolean aliasExists(String indexAlias) {
        try {
            return restClient.indices()
                    .existsAlias(new GetAliasesRequest(indexAlias).masterNodeTimeout(TimeValue.timeValueSeconds(10)), RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to detect if the alias '" + indexAlias + "' exists.", e);
        }
    }

    protected boolean indexExists(String indexName) {
        try {
            final GetIndexRequest request = new GetIndexRequest(indexName);
            return restClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to detect if the index '" + indexName + "' exists.", e);
        }
    }
}
