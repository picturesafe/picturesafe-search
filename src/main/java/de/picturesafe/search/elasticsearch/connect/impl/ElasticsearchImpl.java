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
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchResult;
import de.picturesafe.search.elasticsearch.connect.FacetResolver;
import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.asyncaction.RestClientBulkAction;
import de.picturesafe.search.elasticsearch.connect.asyncaction.RestClientDeleteAction;
import de.picturesafe.search.elasticsearch.connect.asyncaction.RestClientIndexAction;
import de.picturesafe.search.elasticsearch.connect.asyncaction.RestClientIndexRefreshAction;
import de.picturesafe.search.elasticsearch.connect.asyncaction.RestClientSearchAction;
import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryFacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.error.AliasAlreadyExistsException;
import de.picturesafe.search.elasticsearch.connect.error.AliasCreateException;
import de.picturesafe.search.elasticsearch.connect.error.AliasHasMoreThanOneIndexException;
import de.picturesafe.search.elasticsearch.connect.error.ElasticExceptionCause;
import de.picturesafe.search.elasticsearch.connect.error.ElasticsearchException;
import de.picturesafe.search.elasticsearch.connect.error.IndexCreateException;
import de.picturesafe.search.elasticsearch.connect.error.IndexMissingException;
import de.picturesafe.search.elasticsearch.connect.error.QuerySyntaxException;
import de.picturesafe.search.elasticsearch.connect.facet.AggregationBuilderFactories;
import de.picturesafe.search.elasticsearch.connect.facet.AggregationBuilderFactory;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.filter.util.NullAwareAndFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.query.QueryFactory;
import de.picturesafe.search.elasticsearch.connect.query.QueryFactoryCaller;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.connect.util.ElasticExceptionUtils;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.elasticsearch.connect.util.StringTrimUtility;
import de.picturesafe.search.elasticsearch.connect.util.logging.SearchResponseToString;
import de.picturesafe.search.elasticsearch.connect.util.logging.SearchSourceBuilderToString;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.SuggestExpression;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.util.logging.StopWatchPrettyPrint;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.error.ElasticExceptionCause.Type.QUERY_SYNTAX;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticRequestUtils.getRefreshPolicy;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.isTextField;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.keywordFieldName;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.sortFieldName;

@Component
@SuppressWarnings({"unused"})
public class ElasticsearchImpl implements Elasticsearch, QueryFactoryCaller, InitializingBean, TimeZoneAware {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchImpl.class);

    protected ElasticsearchAdmin elasticsearchAdmin;
    protected RestClientConfiguration restClientConfiguration;
    protected RestHighLevelClient restClient;
    protected List<FilterFactory> filterFactories;
    protected List<QueryFactory> queryFactories;
    protected AggregationBuilderFactories aggregationBuilderFactories;
    protected List<FacetResolver> facetResolvers;
    protected String timeZone;

    @Value("${elasticsearch.service.check_cluster_status_timeout:10000}")
    protected long checkClusterStatusTimeout;
    @Value("${elasticsearch.service.indexing_bulk_size:1000}")
    protected int indexingBulkSize;
    @Value("${elasticsearch.service.missing_value_sort_position:LAST}")
    protected MissingValueSortPosition missingValueSortPosition;

    @Autowired
    public ElasticsearchImpl(ElasticsearchAdmin elasticsearchAdmin,
                             RestClientConfiguration restClientConfiguration,
                             List<QueryFactory> queryFactories,
                             List<FilterFactory> filterFactories,
                             @Qualifier("elasticsearchTimeZone") String timeZone) {
        this.elasticsearchAdmin = elasticsearchAdmin;
        this.restClientConfiguration = restClientConfiguration;
        this.queryFactories = queryFactories;
        this.filterFactories = filterFactories;
        this.timeZone = timeZone;
    }

    @Autowired(required = false)
    public void setAggregationBuilderFactories(AggregationBuilderFactories aggregationBuilderFactories) {
        this.aggregationBuilderFactories = aggregationBuilderFactories;
    }

    @Autowired(required = false)
    public void setFacetResolvers(List<FacetResolver> facetResolvers) {
        this.facetResolvers = facetResolvers;
    }

    public void setCheckClusterStatusTimeout(long checkClusterStatusTimeout) {
        this.checkClusterStatusTimeout = checkClusterStatusTimeout;
    }

    public void setIndexingBulkSize(int indexingBulkSize) {
        this.indexingBulkSize = indexingBulkSize;
    }

    public void setMissingValueSortPosition(MissingValueSortPosition missingValueSortPosition) {
        this.missingValueSortPosition = missingValueSortPosition;
    }

    @Override
    public void afterPropertiesSet() {
        this.restClient = restClientConfiguration.getClient();
    }

    @Override
    public void addToIndex(Map<String, Object> document, MappingConfiguration mappingConfiguration, String indexAlias, boolean applyIndexRefresh) {
        Validate.notNull(mappingConfiguration, "Parameter 'mappingConfiguration' may not be null!");
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be empty!");
        try {
            final IndexRequest indexRequest = createIndexRequest(document, mappingConfiguration, indexAlias, applyIndexRefresh);

            final IndexResponse indexResponse = new RestClientIndexAction().action(restClient, indexRequest);
            if (indexResponse.status() != RestStatus.CREATED && indexResponse.status() != RestStatus.OK) {
                throw new ElasticsearchException(
                        "Adding document to index '" + indexAlias + "' failed with response: " + indexResponse.status().getStatus());
            }
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to add document to index '" + indexAlias + "'!", e);
        }
    }

    @Override
    public Map<String, Boolean> addToIndex(List<Map<String, Object>> docs, MappingConfiguration mappingConfiguration, String indexAlias,
                                         boolean applyIndexRefresh, boolean execeptionOnFailure) {
        Validate.notNull(mappingConfiguration, "Parameter 'mappingConfiguration' may not be null!");
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be empty!");

        final Map<String, Boolean> results = new HashMap<>();
        if (CollectionUtils.isEmpty(docs)) {
            return results;
        }

        final StopWatch sw = new StopWatch("index");
        try {
            BulkRequest bulkRequest = null;
            final int size = docs.size();
            for (int i = 0; i < size; i++) {
                if (bulkRequest == null) {
                    bulkRequest = new BulkRequest();
                    bulkRequest.setRefreshPolicy(getRefreshPolicy(applyIndexRefresh));
                }

                final Map<String, Object> doc = docs.get(i);
                final IndexRequest indexRequest = createIndexRequest(doc, mappingConfiguration, indexAlias, false);
                bulkRequest.add(indexRequest);
                if (bulkRequest.numberOfActions() > indexingBulkSize || i == size - 1) {
                    LOG.debug("Adding {} documents to index '{}'.", bulkRequest.numberOfActions(), indexAlias);

                    sw.start("add");
                    final BulkResponse bulkResponse = new RestClientBulkAction().action(restClient, bulkRequest);
                    if (execeptionOnFailure && bulkResponse.hasFailures()) {
                        throw new ElasticsearchException("Add to index failed: " + bulkResponse.buildFailureMessage());
                    }
                    bulkResponse.forEach(itemResponse -> results.put(itemResponse.getId(), itemResponse.getFailure() == null));
                    sw.stop();

                    bulkRequest = null;
                }
            }
            LOG.debug("{}", new StopWatchPrettyPrint(sw));

            return results;
        } catch (IOException e) {
            throw new RuntimeException("Failed to index document for elasticsearch", e);
        }
    }

    @Override
    public void removeFromIndex(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration,
                                boolean applyIndexRefresh, long id) {
        Validate.notNull(mappingConfiguration, "The argument 'indexConfiguration' is null.");

        final String index = indexPresetConfiguration.getIndexAlias();

        final DeleteRequest deleteRequest = new DeleteRequest(index, String.valueOf(id)).setRefreshPolicy(getRefreshPolicy(applyIndexRefresh));
        final DeleteResponse deleteResponse = new RestClientDeleteAction().action(restClient, deleteRequest);

        if (LOG.isDebugEnabled()) {
            LOG.debug("ElasticSearch Client Response: " + deleteResponse);
        }
    }

    @Override
    public void removeFromIndex(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration,
                                boolean applyIndexRefresh, Collection<Long> ids) {
        Validate.notNull(mappingConfiguration, "The argument 'indexConfiguration' is null.");
        Validate.notNull(indexPresetConfiguration, "The argument 'indexPresetConfiguration' is null.");


        if (ids == null || ids.isEmpty()) {
            return;
        }

        final Long[] idsAsArray = ids.toArray(new Long[0]);

        final int maxSize = 10000;
        if (idsAsArray.length == 1) {
            removeFromIndex(mappingConfiguration, indexPresetConfiguration, applyIndexRefresh, idsAsArray[0]);
        } else {
            final String index = indexPresetConfiguration.getIndexAlias();

            // Execution of the deletion in a loop to avoid OutOfMemory problems
            int count = 0;
            do {
                // Size of next bulk
                final int size = Math.min(maxSize, idsAsArray.length - count);
                final BulkRequest bulkRequest = new BulkRequest().setRefreshPolicy(getRefreshPolicy(applyIndexRefresh));
                for (int i = count; i < count + size; i++) {
                    bulkRequest.add(new DeleteRequest(index, String.valueOf(idsAsArray[i])));
                }

                final BulkResponse bulkResponse = new RestClientBulkAction().action(restClient, bulkRequest);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ElasticSearch Client Response: " + bulkResponse);
                }

                count += size;
            } while (count < idsAsArray.length);
        }
    }

    @Override
    public boolean isServiceAvailable() {
        LOG.info("Check for cluster status YELLOW.");
        final boolean success = elasticsearchAdmin.waitForMinStatus(null, ClusterHealthStatus.YELLOW, checkClusterStatusTimeout);
        if (success) {
            LOG.info("Cluster is ok!");
        } else {
            LOG.error("Checking status of cluster failed!");
        }

        return success;
    }

    @Override
    public void refresh(String indexAlias) {
        final RefreshRequest request = new RefreshRequest(indexAlias);
        try {
            new RestClientIndexRefreshAction().action(restClient, request);
            restClient.indices().refresh(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Failed to refresh the index '" + indexAlias + "'");
        }
    }

    @Override
    public String createIndex(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration) throws IndexCreateException {
        return elasticsearchAdmin.createIndex(indexPresetConfiguration, mappingConfiguration);
    }

    @Override
    public String createIndexWithAlias(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration)
            throws IndexCreateException, AliasCreateException, AliasAlreadyExistsException {
        return elasticsearchAdmin.createIndexWithAlias(indexPresetConfiguration, mappingConfiguration);
    }

    @Override
    public void deleteIndex(String indexName) {
        elasticsearchAdmin.deleteIndex(indexName);
    }

    @Override
    public void createAlias(String indexAlias, String indexName) throws AliasCreateException, AliasAlreadyExistsException {
        elasticsearchAdmin.createAlias(indexAlias, indexName);
    }

    @Override
    public String removeAlias(IndexPresetConfiguration indexPresetConfiguration) throws AliasHasMoreThanOneIndexException {
        return elasticsearchAdmin.removeAlias(indexPresetConfiguration.getIndexAlias());
    }

    @Override
    public boolean aliasExists(String indexAlias) {
        return elasticsearchAdmin.aliasOrIndexExists(indexAlias);
    }

    @Override
    public void setIndexVersion(String indexAlias, int indexVersion, MappingConfiguration mappingConfiguration) {
        final Map<String, Object> document = new HashMap<>();
        document.put("id", 0);
        document.put(INDEX_VERSION, indexVersion);
        addToIndex(document, mappingConfiguration, indexAlias, true);
    }

    @Override
    public int getIndexVersion(String indexAlias) {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().query(QueryBuilders.existsQuery(INDEX_VERSION)).docValueField(INDEX_VERSION);
        try {
            final SearchResponse searchResponse = restClient.search(new SearchRequest(indexAlias).source(sourceBuilder), RequestOptions.DEFAULT);
            for (final SearchHit searchHit : searchResponse.getHits().getHits()) {
                final DocumentField field = searchHit.getFields().get(INDEX_VERSION);
                if (field != null) {
                    final Object value = field.getValue();
                    if (value == null) {
                        return -1;
                    }
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                    return (int) value;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed get the index version.", e);
        }

        return -1;
    }

    @Override
    public List<String> resolveIndexNames(String indexAlias) {
        return elasticsearchAdmin.resolveIndexNames(indexAlias);
    }

    @Override
    public void updateMapping(IndexPresetConfiguration indexPresetConfiguration, MappingConfiguration mappingConfiguration,
                              List<FieldConfiguration> fieldConfigs) {
        elasticsearchAdmin.updateMapping(indexPresetConfiguration, mappingConfiguration, fieldConfigs);
    }

    @Override
    public ElasticsearchInfo getElasticsearchInfo() {

        final ElasticsearchInfo elasticsearchInfo = new ElasticsearchInfo();
        elasticsearchInfo.setClientVersion(Version.CURRENT.toString());
        try {
            final MainResponse mainResponse = restClient.info(RequestOptions.DEFAULT);
            elasticsearchInfo.setServerVersion(mainResponse.getVersion().getNumber());
            elasticsearchInfo.setClusterName(mainResponse.getClusterName());
        } catch (IOException ioe) {
            throw new RuntimeException("Getting elasticsearch info failed!", ioe);
        }
        return elasticsearchInfo;
    }

    @Override
    public ElasticsearchResult search(final QueryDto queryDto, final MappingConfiguration mappingConfiguration,
                                      IndexPresetConfiguration indexPresetConfiguration) {
        return new WatchedTask<ElasticsearchResult>(LOG, "search") {
            @Override
            public ElasticsearchResult process() {
                try {
                    final List<Map<String, Object>> result = new ArrayList<>();
                    final SearchResponse searchResponse = internalSearch(queryDto, mappingConfiguration, indexPresetConfiguration);
                    final SearchHits searchHits = searchResponse.getHits();
                    final SearchHit[] hits = searchHits.getHits();
                    final TotalHits totalHits = searchHits.getTotalHits();

                    for (SearchHit hit : hits) {
                        final Map<String, Object> source = hit.getSourceAsMap();
                        final Map<String, DocumentField> documentField = hit.getFields();
                        final Map<String, Object> backMappedSource = convertFields(source, documentField, mappingConfiguration);
                        result.add(backMappedSource);
                    }
                    final List<FacetDto> facetDtos = convertFacets(searchResponse, queryDto, mappingConfiguration);

                    return new ElasticsearchResult(totalHits.value, totalHits.relation == TotalHits.Relation.EQUAL_TO, result, facetDtos);
                } catch (IndexMissingException e) {
                    throw new IndexMissingException(indexPresetConfiguration.getIndexAlias());
                }
            }
        }.getResult();
    }

    @Override
    public Map<String, Object> getDocument(String indexAlias, long id) {
        try {
            final GetRequest request = new GetRequest().index(indexAlias).id(String.valueOf(id));
            final GetResponse response = restClient.get(request, RequestOptions.DEFAULT);
            return response.getSource();
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to get document: id=" + id, e);
        }
    }

    @Override
    public Map<String, List<String>> suggest(String indexAlias, SuggestExpression... expressions) {
        try {
            final SuggestBuilder suggestBuilder = new SuggestBuilder();
            for (final SuggestExpression expression : expressions) {
                suggestBuilder.addSuggestion(expression.getName(),
                        new CompletionSuggestionBuilder(expression.getName()).prefix(expression.getText()).size(expression.getCount()).skipDuplicates(true));
            }

            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().suggest(suggestBuilder);
            final SearchRequest searchRequest = new SearchRequest(indexAlias).source(searchSourceBuilder);
            final SearchResponse searchResponse = new RestClientSearchAction().action(restClient, searchRequest);

            final Map<String, List<String>> result = new HashMap<>();
            for (final Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> suggestion
                    : searchResponse.getSuggest()) {
                final List<String> suggestedLines = result.computeIfAbsent(suggestion.getName(), name -> new ArrayList<>());
                for (final Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : suggestion.getEntries()) {
                    for (final Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                        suggestedLines.add(option.getText().string());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new ElasticsearchException("Failed to get suggestions: alias = " + indexAlias, e);
        }
    }

    @Override
    public QueryBuilder createQuery(Expression expression, MappingConfiguration mappingConfiguration) {
        for (QueryFactory queryFactory : queryFactories) {
            if (queryFactory.supports(expression)) {
                final QueryBuilder result = queryFactory.create(this, mappingConfiguration, expression);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected Map<String, Object> convertFields(Map<String, Object> source, Map<String, DocumentField> fields, MappingConfiguration mappingConfiguration) {
        final Map<String, Object> result = new HashMap<>();
        if (source != null) {
            for (Map.Entry<String, Object> entry : source.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                result.put(key, value);
            }
        } else if (fields != null) {
            for (Map.Entry<String, DocumentField> field : fields.entrySet()) {
                final String key = field.getKey();
                final DocumentField documentField = field.getValue();
                final Object value = documentField.getValue();
                result.put(key, value);
            }
        } else {
            throw new RuntimeException("Missing data in search result!");
        }

        return result;
    }

    protected List<FacetDto> convertFacets(SearchResponse searchResponse, QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final FacetConverter facetConverter = new FacetConverter(queryDto);
        final List<FacetDto> facetResultList = new ArrayList<>();

        if (searchResponse.getAggregations() != null) {
            for (Aggregation aggregation : searchResponse.getAggregations()) {
                FacetDto facetDto = null;
                if (aggregation instanceof Terms) {
                    facetDto = facetConverter.convertTermsFacet((Terms) aggregation, facetResolver(aggregation));
                } else if (aggregation instanceof Range) {
                    facetDto = facetConverter.convertRangeFacet((Range) aggregation, facetResolver(aggregation));
                } else if (aggregation instanceof Histogram) {
                    facetDto = facetConverter.convertHistogramFacet((Histogram) aggregation, facetResolver(aggregation));
                }

                if (facetDto != null) {
                    facetResultList.add(facetDto);
                }
            }
        }
        return facetResultList;
    }

    protected FacetResolver facetResolver(Aggregation aggregation) {
        if (facetResolvers != null) {
            for (FacetResolver facetResolver : facetResolvers) {
                if (facetResolver.isResponsible(aggregation.getName())) {
                    return facetResolver;
                }
            }
        }
        return null;
    }

    protected SearchResponse internalSearch(QueryDto queryDto, MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration) {
        final SearchSourceBuilder searchSourceBuilder = searchSourceBuilder(queryDto);

        final QueryBuilder filterBuilder = createFilter(queryDto, mappingConfiguration);
        final QueryBuilder queryBuilder = createQuery(queryDto.getExpression(), mappingConfiguration);

        if (filterBuilder != null) {
            if (queryBuilder == null) {
                searchSourceBuilder.query(QueryBuilders.boolQuery().filter(filterBuilder));
            } else {
                searchSourceBuilder.query(QueryBuilders.boolQuery().must(queryBuilder).filter(filterBuilder));
            }
        } else if (queryBuilder != null) {
            searchSourceBuilder.query(queryBuilder);
        }

        addSortOptionsToSearchRequest(queryDto, mappingConfiguration, searchSourceBuilder);
        addFacetsToSearchRequest(queryDto, mappingConfiguration, searchSourceBuilder);
        addFieldsToSearchRequest(queryDto, mappingConfiguration, searchSourceBuilder);

        LOG.debug("Search request:\n{}", new SearchSourceBuilderToString(searchSourceBuilder));

        final SearchRequest searchRequest = new SearchRequest(indexPresetConfiguration.getIndexAlias());
        searchRequest.source(searchSourceBuilder);

        final SearchResponse searchResponse;
        try {
            searchResponse = new RestClientSearchAction().action(restClient, searchRequest);
        } catch (Exception e) {
            final ElasticExceptionCause cause = ElasticExceptionUtils.getCause(e);
            if (QUERY_SYNTAX == cause.getType()) {
                throw new QuerySyntaxException("Elasticsearch rest client search action failed: Failed to parse query!", cause.getMessage(), e);
            } else {
                throw new ElasticsearchException("Elasticsearch rest client search action failed!", e);
            }
        }

        LOG.debug("Search response:\n{},", new SearchResponseToString(searchResponse));
        return searchResponse;
    }

    protected void addFacetsToSearchRequest(QueryDto queryDto, MappingConfiguration mappingConfiguration, SearchSourceBuilder searchRequestBuilder) {
        if (queryDto.getQueryFacetDtos() != null) {
            for (QueryFacetDto queryFacetDto : queryDto.getQueryFacetDtos()) {
                final String fieldName = queryFacetDto.getField();
                List<AggregationBuilderFactory> fieldAggregationBuilderFactories = null;
                if (aggregationBuilderFactories != null) {
                    fieldAggregationBuilderFactories
                            = aggregationBuilderFactories.getAggregationBuilderFactories(mappingConfiguration, queryFacetDto.getField());
                }

                final List<AggregationBuilder> aggregationBuilders = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(fieldAggregationBuilderFactories)) {
                    for (final AggregationBuilderFactory factory : fieldAggregationBuilderFactories) {
                        aggregationBuilders.add(factory.createAggregationBuilder(fieldName, queryFacetDto.getSize(), queryDto.getLocale().getLanguage()));
                    }
                } else {
                    final String aggregationName = StringUtils.substringBefore(fieldName, ".");
                    final TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(aggregationName);

                    final FieldConfiguration fieldConfig = mappingConfiguration.getFieldConfiguration(fieldName);
                    if (fieldConfig == null) {
                        LOG.warn("Missing field configuration for field '{}', aggregations will not work for text fields!", fieldName);
                    }

                    String aggFieldName = FieldConfigurationUtils.getElasticFieldName(mappingConfiguration, queryFacetDto.getField(), queryDto.getLocale());
                    aggFieldName = keywordFieldName(fieldConfig, aggFieldName);
                    termsBuilder.field(aggFieldName).size(queryFacetDto.getSize());
                    termsBuilder.field(aggFieldName).shardSize(queryFacetDto.getShardSize());
                    aggregationBuilders.add(termsBuilder);
                }

                for (final AggregationBuilder aggregationBuilder : aggregationBuilders) {
                    searchRequestBuilder.aggregation(aggregationBuilder);
                }
            }
        }
    }

    protected void addSortOptionsToSearchRequest(QueryDto queryDto, MappingConfiguration mappingConfig, SearchSourceBuilder searchRequestBuilder) {
        if (queryDto.getSortOptions() != null) {
            for (SortOption sortOption : queryDto.getSortOptions()) {
                final String fieldName = sortOption.getFieldName();
                FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfig, fieldName, false);
                final String topFieldName = StringUtils.substringBefore(fieldName, ".");

                if (fieldConfiguration == null) {
                    fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfig, topFieldName, false);
                }

                FieldSortBuilder fieldSortBuilder = null;
                if (fieldConfiguration != null) {
                    if (fieldConfiguration.getParent() != null) {
                        fieldConfiguration = fieldConfiguration.getParent();
                    }

                    if (fieldConfiguration.isNestedObject()) {
                        fieldSortBuilder = buildNestedSort(queryDto, fieldConfiguration, fieldName, sortOption, mappingConfig);
                    } else if (isTextField(fieldConfiguration)) {
                        fieldSortBuilder = buildStringSort(fieldConfiguration, mappingConfig, fieldName, sortOrder(sortOption), queryDto.getLocale());
                    }
                } else {
                    LOG.warn("Missing field configuration for field '{}', sorting by this field may not be possible.", fieldName);
                }

                if (fieldSortBuilder == null) {
                    fieldSortBuilder = SortBuilders.fieldSort(topFieldName).order(sortOrder(sortOption)).missing(sortMissing());
                }
                searchRequestBuilder.sort(fieldSortBuilder);
            }
        }
    }

    private FieldSortBuilder buildNestedSort(QueryDto queryDto, FieldConfiguration fieldConfiguration, String nestedFieldName, SortOption sortOption,
                                             MappingConfiguration mappingConfiguration) {
        final FieldConfiguration nestedField = fieldConfiguration.getNestedField(StringUtils.substringAfter(nestedFieldName, "."));
        final String sortFieldName = sortFieldName(nestedField, nestedFieldName);
        return SortBuilders
                .fieldSort(sortFieldName)
                .order(sortOrder(sortOption))
                .missing(sortMissing())
                .setNestedSort(nestedSortBuilder(queryDto, fieldConfiguration.getName(), sortOption, mappingConfiguration));
    }

    private NestedSortBuilder nestedSortBuilder(QueryDto queryDto, String topFieldName, SortOption sortOption, MappingConfiguration mappingConfiguration) {
        final NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(topFieldName);
        if (sortOption.getFilter() != null) {
            nestedSortBuilder.setFilter(createFilter(QueryDto.sortFilter(sortOption.getFilter(), queryDto.getLocale()), mappingConfiguration));
        }
        return nestedSortBuilder;
    }

    private SortOrder sortOrder(SortOption sortOption) {
        return (sortOption.getSortDirection() == SortOption.Direction.ASC) ? SortOrder.ASC : SortOrder.DESC;
    }

    private String sortMissing() {
        return "_" + missingValueSortPosition.getValue();
    }

    protected FieldSortBuilder buildStringSort(FieldConfiguration fieldConfiguration, MappingConfiguration mappingConfiguration, String fieldName,
                                             SortOrder sortOrder, Locale locale) {
        if (fieldConfiguration.isSortable()) {
            final String esFieldName = FieldConfigurationUtils.getElasticFieldName(mappingConfiguration, fieldName, locale);
            final String sortFieldName = sortFieldName(fieldConfiguration, esFieldName);
            return SortBuilders
                    .fieldSort(sortFieldName)
                    .missing("_" + missingValueSortPosition.getValue())
                    .order(sortOrder);
        } else {
            throw new RuntimeException("The field '" + fieldConfiguration.getName() + "' is not configured as sortable!");
        }
    }

    protected void addFieldsToSearchRequest(QueryDto queryDto, MappingConfiguration mappingConfiguration, SearchSourceBuilder searchRequestBuilder) {
        final List<String> fields = queryDto.getFieldsToResolve();
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }

        validateFields(queryDto, mappingConfiguration);

        switch (queryDto.getFieldResolverType()) {
            case DOC_VALUES:
                addDocValuesToSearchRequest(fields, searchRequestBuilder);
                break;
            case SOURCE_VALUES:
                addSourceValuesToSearchRequest(fields, searchRequestBuilder);
                break;
        }
    }

    protected void validateFields(QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final Map<String, FieldConfiguration> fieldConfigurations = new HashMap<>(mappingConfiguration.getFieldConfigurations().size());
        for (final FieldConfiguration fieldConfiguration : mappingConfiguration.getFieldConfigurations()) {
            fieldConfigurations.put(fieldConfiguration.getName(), fieldConfiguration);
        }

        for (final String field : queryDto.getFieldsToResolve()) {
            final FieldConfiguration fieldConfiguration = fieldConfigurations.get(field);
            if (fieldConfiguration == null) {
                throw new RuntimeException("Undefined field to resolve: " + field);
            }
        }
    }

    protected void addDocValuesToSearchRequest(List<String> fields, SearchSourceBuilder searchRequestBuilder) {
        searchRequestBuilder.fetchSource(false);
        for (final String field : fields) {
            searchRequestBuilder.docValueField(field);
        }
    }

    protected void addSourceValuesToSearchRequest(List<String> fields, SearchSourceBuilder searchRequestBuilder) {
        if (!fields.contains("id")) {
            fields.add("id"); // mandatory for elasticsearch service
        }
        final String[] includes = fields.toArray(new String[0]);
        final String[] excludes = new String[0];
        searchRequestBuilder.fetchSource(includes, excludes);
    }

    protected QueryBuilder createFilter(QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final List<QueryBuilder> queryBuilders = new ArrayList<>();
        for (FilterFactory filterFactory : filterFactories) {
            final List<QueryBuilder> filters = filterFactory.create(queryDto, mappingConfiguration);
            if (CollectionUtils.isNotEmpty(filters)) {
                queryBuilders.addAll(filters);
            }
        }
        final NullAwareAndFilterBuilder allFilterBuilder = new NullAwareAndFilterBuilder();
        for (QueryBuilder queryBuilder : queryBuilders) {
            allFilterBuilder.add(queryBuilder);
        }

        return allFilterBuilder.toFilterBuilder();
    }

    protected SearchSourceBuilder searchSourceBuilder(QueryDto queryDto) {
        final QueryRangeDto queryRangeDto = queryDto.getQueryRangeDto();
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().from(queryRangeDto.getStart()).size(queryRangeDto.getLimit());
        if (queryRangeDto.getMaxTrackTotalHits() != null) {
            searchSourceBuilder.trackTotalHitsUpTo(queryRangeDto.getMaxTrackTotalHits().intValue());
        }
        return searchSourceBuilder;
    }

    protected IndexRequest createIndexRequest(Map<String, Object> doc, MappingConfiguration mappingConfiguration, String indexName, boolean applyIndexRefresh)
            throws IOException {
        final XContentBuilder contentBuilder = XContentFactory.jsonBuilder();
        contentBuilder.startObject();
        addToIndexRequestContent(contentBuilder, doc, mappingConfiguration);
        contentBuilder.endObject();
        final Long id = getId(doc);
        final String docId = (id != null) ? String.valueOf(id) : null;
        final IndexRequest indexRequest = new IndexRequest(indexName).id(docId)
                .source(contentBuilder).setRefreshPolicy(getRefreshPolicy(applyIndexRefresh));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to index " + indexRequest.toString());
        }

        return indexRequest;
    }

    @SuppressWarnings("unchecked")
    protected void addToIndexRequestContent(XContentBuilder contentBuilder, Map<String, Object> doc,
                                          MappingConfiguration mappingConfiguration) throws IOException {
        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            if (entry.getValue() != null) {
                final String fieldName = entry.getKey();
                if (entry.getValue() instanceof List) {
                    final List<?> list = (List<?>) entry.getValue();
                    if (list.size() > 0 && list.get(0) instanceof Map) {
                        final List<Map<String, Object>> nestedObjectList = (List<Map<String, Object>>) list;
                        addNestedObjectsToIndexRequestContent(contentBuilder, fieldName, nestedObjectList, mappingConfiguration);
                    } else {
                        final List<?> trimmedList = StringTrimUtility.trimListValues((List<?>) entry.getValue());
                        contentBuilder.array(fieldName, trimmedList.toArray());
                    }
                } else if (entry.getValue() instanceof Date) {
                    contentBuilder.field(fieldName, ElasticDateUtils.formatIso((Date) entry.getValue(), timeZone));
                } else if (entry.getValue() instanceof Boolean) {
                    contentBuilder.field(fieldName, entry.getValue());
                } else if (entry.getValue() instanceof String && (entry.getValue().equals(Boolean.TRUE.toString())
                        || entry.getValue().equals(Boolean.FALSE.toString()))) {
                    contentBuilder.field(fieldName, Boolean.valueOf((String) entry.getValue()));
                } else if (entry.getValue() instanceof String) {
                    final String stringValue = (String) entry.getValue();
                    contentBuilder.field(fieldName, stringValue.trim());
                } else {
                    contentBuilder.field(fieldName, entry.getValue());
                }
            }
        }
    }

    protected void addNestedObjectsToIndexRequestContent(XContentBuilder contentBuilder, String fieldName, List<Map<String, Object>> nestedObjectList,
                                                       MappingConfiguration mappingConfiguration) throws IOException {
        contentBuilder.startArray(fieldName);
        for (final Map<String, Object> doc : nestedObjectList) {
            contentBuilder.startObject();
            addToIndexRequestContent(contentBuilder, doc, mappingConfiguration);
            contentBuilder.endObject();
        }
        contentBuilder.endArray();
    }
}
