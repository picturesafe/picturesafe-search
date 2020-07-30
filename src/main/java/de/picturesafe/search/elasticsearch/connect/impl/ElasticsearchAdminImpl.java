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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Component
public class ElasticsearchAdminImpl implements ElasticsearchAdmin {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchAdminImpl.class);

    protected static final String CHAR_FILTER_UMLAUT_MAPPING = "umlaut_mapping";
    protected static final String FILTER_WORD_DELIMITER = "filter_word_delimiter";

    protected RestClientConfiguration restClientConfiguration;
    protected RestHighLevelClient restClient;

    @Autowired
    public ElasticsearchAdminImpl(RestClientConfiguration restClientConfiguration) {
        this.restClientConfiguration = restClientConfiguration;
    }

    @PostConstruct
    public void init() {
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
    public Map<String, List<String>> listIndices() {
        final Map<String, List<String>> result = new TreeMap<>();
        try {
            final GetAliasesResponse response = restClient.indices().getAlias(new GetAliasesRequest(), RequestOptions.DEFAULT);
            response.getAliases().forEach((indexName, aliasMetaData) -> {
                aliasMetaData.forEach(aliasMeta -> result.computeIfAbsent(indexName, i -> new ArrayList<>()).add(aliasMeta.alias()));
            });
            return result;
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to list indices!", e);
        }
    }

    @Override
    public List<String> resolveIndexNames(String indexAlias) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");

        final GetAliasesRequest getAliasesRequest = new GetAliasesRequest(indexAlias);
        final GetAliasesResponse getAliasesResponse;
        try {
            getAliasesResponse = restClient.indices().getAlias(getAliasesRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to resolve index names: indexAlias=" + indexAlias, e);
        }
        final List<String> indexesWithAlias = new ArrayList<>();
        getAliasesResponse.getAliases().forEach((indexName, aliasMetaData) -> {
            aliasMetaData.forEach(aliasMeta -> {
                if (indexAlias.equals(aliasMeta.alias())) {
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
    public Map<String, Object> getMapping(String indexName) {
        return doGetMapping(indexName).sourceAsMap();
    }

    @Override
    public String getMappingAsJson(String indexName) {
        return doGetMapping(indexName).source().toString();
    }

    private MappingMetaData doGetMapping(String indexName) {
        final GetMappingsRequest request = new GetMappingsRequest();
        request.indices(indexName);
        final GetMappingsResponse getMappingResponse;
        try {
            getMappingResponse = restClient.indices().getMapping(request, RequestOptions.DEFAULT);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to load mapping for index name '" + indexName + "'!", ioe);
        }
        final Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
        return allMappings.get(indexName);
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

            if (hasAnalysisSettings(indexPresetConfiguration)) {
                // start analysis configuration
                result.startObject("analysis");
                addIndexSettings(result, "char_filter", indexPresetConfiguration.getCustomCharFilters());
                addIndexSettings(result, "filter", indexPresetConfiguration.getCustomFilters());
                addIndexSettings(result, "analyzer", indexPresetConfiguration.getCustomAnalyzers());
                addIndexSettings(result, "tokenizer", indexPresetConfiguration.getCustomTokenizers());
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

    protected boolean hasAnalysisSettings(IndexPresetConfiguration indexPresetConfiguration) {
        return CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomCharFilters())
                || CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomFilters())
                || CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomAnalyzers())
                || CollectionUtils.isNotEmpty(indexPresetConfiguration.getCustomTokenizers());
    }

    protected void addIndexSettings(XContentBuilder result, String analysisType, List<IndexSettingsObject> indexSettingsObjects) {
        try {
            result.startObject(analysisType);
            if (CollectionUtils.isNotEmpty(indexSettingsObjects)) {
                for (final IndexSettingsObject settings : indexSettingsObjects) {
                    result.rawField(settings.name(), IOUtils.toInputStream(settings.json(), StandardCharsets.UTF_8), XContentType.JSON);
                }
            }
            result.endObject();
        } catch (Exception e) {
            throw new RuntimeException("Adding analysis index settings failed: " + analysisType, e);
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
