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

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.IndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.api.RangeFacetItem;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.LanguageSortConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import de.picturesafe.search.elasticsearch.connect.dto.FacetDto;
import de.picturesafe.search.elasticsearch.connect.dto.FacetEntryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.dto.QueryRangeDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchHitDto;
import de.picturesafe.search.elasticsearch.connect.dto.SearchResultDto;
import de.picturesafe.search.elasticsearch.error.ElasticsearchServiceException;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.IdFormat;
import de.picturesafe.search.elasticsearch.model.IndexObject;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.ResultRangeFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.SuggestExpression;
import de.picturesafe.search.parameter.AccountContext;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.util.logging.StopWatchPrettyPrint;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("unused")
public class ElasticsearchServiceImpl implements ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceImpl.class);
    protected static final int DEFAULT_PAGE_SIZE = 100;
    protected static final int DEFAULT_MAX_PAGE_SIZE = 2000;

    protected final Elasticsearch elasticsearch;
    protected final IndexPresetConfigurationProvider indexPresetConfigurationProvider;
    protected final FieldConfigurationProvider fieldConfigurationProvider;

    @Value("${elasticsearch.service.default_page_size:" + DEFAULT_PAGE_SIZE + "}")
    protected int defaultPageSize = DEFAULT_PAGE_SIZE;

    @Value("${elasticsearch.service.max_page_size:" + DEFAULT_MAX_PAGE_SIZE + "}")
    protected int maxPageSize = DEFAULT_MAX_PAGE_SIZE;

    @Value("${elasticsearch.service.optimize_expressions.enabled:true}")
    protected boolean optimizeExpressionsEnabled = true;

    protected IdFormat idFormat = IdFormat.DEFAULT;

    @Autowired
    public ElasticsearchServiceImpl(Elasticsearch elasticsearch, IndexPresetConfigurationProvider indexPresetConfigurationProvider,
                                    FieldConfigurationProvider fieldConfigurationProvider) {
        this.elasticsearch = elasticsearch;
        this.indexPresetConfigurationProvider = indexPresetConfigurationProvider;
        this.fieldConfigurationProvider = fieldConfigurationProvider;
    }

    /**
     * Sets the default page size to be retrieved.
     *
     * @param defaultPageSize   The default page size to be retrieved
     */
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    /**
     * Sets the max allowed page size to be retrieved.
     *
     * @param maxPageSize   The max allowed page size to be retrieved
     */
    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * Sets if expressions should be optimized.
     *
     * @see Expression#optimize()
     * @see SearchParameter#isOptimizeExpressions()
     *
     * @param optimizeExpressionsEnabled true if expressions should be optimized
     */
    public void setOptimizeExpressionsEnabled(boolean optimizeExpressionsEnabled) {
        this.optimizeExpressionsEnabled = optimizeExpressionsEnabled;
    }

    @Autowired(required = false)
    public void setIdFormat(IdFormat idFormat) {
        this.idFormat = idFormat;
    }

    @Override
    public ElasticsearchInfo getElasticsearchInfo() {
        return elasticsearch.getElasticsearchInfo();
    }

    @Override
    public boolean aliasExists(String indexAlias) {
        return elasticsearch.aliasExists(indexAlias);
    }

    @Override
    public String createIndex(String indexAlias) {
        LOGGER.info("Creating a new elasticsearch index for alias '{}'", indexAlias);
        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
        final MappingConfiguration mappingConfiguration = getMappingConfiguration(indexAlias, true);
        final String indexName = elasticsearch.createIndex(indexPresetConfiguration, mappingConfiguration);
        LOGGER.info("New elasticsearch index '{}' was created for alias '{}'", indexName, indexAlias);
        return indexName;
    }

    @Override
    public String createIndexWithAlias(String indexAlias) {
        LOGGER.info("Creating a new elasticsearch index with alias '{}'", indexAlias);
        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
        final MappingConfiguration mappingConfiguration = getMappingConfiguration(indexAlias, true);
        final String indexName = elasticsearch.createIndexWithAlias(indexPresetConfiguration, mappingConfiguration);
        LOGGER.info("New elasticsearch index '{}' was created with alias '{}'", indexName, indexAlias);
        return indexName;
    }

    @Override
    public void addFieldConfiguration(String indexAlias, FieldConfiguration... fieldConfigs) {
        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
        final MappingConfiguration mappingConfiguration = getMappingConfiguration(indexAlias, false);
        elasticsearch.updateMapping(indexPresetConfiguration, mappingConfiguration, Arrays.asList(fieldConfigs));
    }

    @Override
    public void deleteIndex(String indexName) {
        LOGGER.info("Deleting elasticsearch index: {}", indexName);
        elasticsearch.deleteIndex(indexName);
    }

    @Override
    public void deleteIndexWithAlias(String indexAlias) {
        LOGGER.info("Deleting elasticsearch indexes for alias: {}", indexAlias);
        for (String indexName : elasticsearch.resolveIndexNames(indexAlias)) {
            deleteIndex(indexName);
        }
        removeAlias(indexAlias);
    }

    @Override
    public List<String> resolveIndexNames(String indexAlias) {
        try {
            return elasticsearch.resolveIndexNames(indexAlias);
        } catch (Exception e) {
            throw new ElasticsearchServiceException("Failed to resolve index names for alis: " + indexAlias, e);
        }
    }

    @Override
    public void createAlias(String indexAlias, String indexName) {
        LOGGER.info("Creating elasticsearch alias '{}' for index '{}'", indexAlias, indexName);
        elasticsearch.createAlias(indexAlias, indexName);
    }

    @Override
    public String removeAlias(String indexAlias) {
        if (elasticsearch.aliasExists(indexAlias)) {
            LOGGER.info("Removing elasticsearch alias '{}'", indexAlias);
            final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
            return elasticsearch.removeAlias(indexPresetConfiguration);
        } else {
            return null;
        }
    }

    @Override
    public void setIndexVersion(String indexAlias, int indexVersion) {
        elasticsearch.setIndexVersion(indexAlias, indexVersion);
    }

    @Override
    public int getIndexVersion(String indexAlias) {
        return elasticsearch.getIndexVersion(indexAlias);
    }

    @Override
    public void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Map<String, Object> document) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notNull(dataChangeProcessingMode, "Parameter 'dataChangeProcessingMode' may not be null!");
        Validate.notNull(document, "Parameter 'document' may not be null!");

        elasticsearch.addToIndex(indexAlias, dataChangeProcessingMode.isRefresh(), document);
    }

    @Override
    public void addObjectToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object) {
        Validate.notNull(object, "Parameter 'object' may not be null!");
        addToIndex(indexAlias, dataChangeProcessingMode, object.toDocument());
    }

    @Override
    public void addObjectToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object, Object id) {
        Validate.notNull(object, "Parameter 'object' may not be null!");
        final Map<String, Object> doc = object.toDocument();
        doc.put("id", idFormat.format(id));
        addToIndex(indexAlias, dataChangeProcessingMode, doc);
    }

    @Override
    public void addToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, List<Map<String, Object>> documents) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notNull(dataChangeProcessingMode, "Parameter 'dataChangeProcessingMode' may not be null!");
        Validate.notNull(documents, "Parameter 'documents' may not be null!");

        elasticsearch.addToIndex(indexAlias, dataChangeProcessingMode.isRefresh(), true, documents);
    }

    @Override
    public void addObjectsToIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, List<IndexObject<?>> objects) {
        Validate.notNull(objects, "Parameter 'objects' may not be null!");
        addToIndex(indexAlias, dataChangeProcessingMode, objects.stream().map(IndexObject::toDocument).collect(Collectors.toList()));
    }

    @Override
    public void removeFromIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Object id) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notNull(dataChangeProcessingMode, "Parameter 'dataChangeProcessingMode' may not be null!");

        elasticsearch.removeFromIndex(indexAlias, dataChangeProcessingMode.isRefresh(), id);
    }

    @Override
    public void removeFromIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Collection<?> ids) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notNull(dataChangeProcessingMode, "Parameter 'dataChangeProcessingMode' may not be null!");
        Validate.notNull(ids, "Parameter 'ids' may not be null!");

        elasticsearch.removeFromIndex(indexAlias, dataChangeProcessingMode.isRefresh(), ids);
    }

    @Override
    public void removeFromIndex(String indexAlias, DataChangeProcessingMode dataChangeProcessingMode, Expression expression, Locale locale) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notNull(dataChangeProcessingMode, "Parameter 'dataChangeProcessingMode' may not be null!");
        Validate.notNull(expression, "Parameter 'expression' may not be null!");
        Validate.notNull(locale, "Parameter 'locale' may not be null!");

        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
        elasticsearch.removeFromIndex(new QueryDto(expression, locale), getMappingConfiguration(indexAlias, true), indexPresetConfiguration,
                dataChangeProcessingMode.isRefresh());
    }

    @Override
    public SearchResult search(String indexAlias, Expression expression, SearchParameter searchParameter) {
        return search(indexAlias, new AccountContext<>(), expression, searchParameter);
    }

    @Override
    public SearchResult search(String indexAlias, AccountContext<?> accountContext, Expression expression, SearchParameter searchParameter) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");

        final StopWatch sw = new StopWatch();

        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
        final int pageSize = getPageSize(searchParameter);
        final SearchResultDto searchResultDto
                = getElasticsearchResult(new InternalSearchContext(indexPresetConfiguration, accountContext, expression, searchParameter, pageSize), sw);
        final List<SearchResultItem> resultItems = searchResultDto.getHits().stream().map(this::searchResultItem).collect(Collectors.toList());

        sw.start("get max results");
        final long totalHitCount = searchResultDto.getTotalHitCount();
        final int resultCount = getMaxResults(indexAlias, searchParameter.getMaxResults(), totalHitCount);
        sw.stop();

        LOGGER.debug("Performed search on index '{}':\n{}", indexAlias, new StopWatchPrettyPrint(sw));
        final int pageIndex = (searchParameter.getPageIndex() != null) ? searchParameter.getPageIndex() : 1;
        return new SearchResult(resultItems, pageIndex, pageSize, resultCount, totalHitCount, searchResultDto.isExactCount(),
                convertFacets(searchResultDto.getFacetDtoList()));
    }

    protected SearchResultItem searchResultItem(SearchHitDto hit) {
        return new SearchResultItem(hit.getId(), hit.getAttributes(), idFormat).innerHits(convertInnerHits(hit.getInnerHits()));
    }

    protected Map<String, List<SearchResultItem>> convertInnerHits(Map<String, List<SearchHitDto>> innerHits) {
        if (MapUtils.isNotEmpty(innerHits)) {
            final Map<String, List<SearchResultItem>> convertedHits = new TreeMap<>();
            innerHits.forEach((name, hits) -> {
                hits.forEach(hit -> convertedHits.computeIfAbsent(name, k -> new ArrayList<>()).add(searchResultItem(hit)));
            });
            return convertedHits;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> getDocument(String indexAlias, Object id) {
        return elasticsearch.getDocument(indexAlias, id);
    }

    @Override
    public <T extends IndexObject<T>> T getObject(String indexAlias, Object id, Class<T> type) {
        final Map<String, Object> doc = getDocument(indexAlias, id);
        return (doc != null) ? IndexObject.fromDocument(doc, type) : null;
    }

    @Override
    public SuggestResult suggest(String indexAlias, SuggestExpression... expressions) {
        Validate.notEmpty(indexAlias, "Parameter 'indexAlias' may not be null or empty!");
        Validate.notEmpty(expressions, "Parameter 'expressions' may not be null or empty!");
        return new SuggestResult(elasticsearch.suggest(indexAlias, expressions));
    }

    protected int getPageSize(SearchParameter searchParameter) {
        if (searchParameter == null) {
            return defaultPageSize;
        }
        int pageSize = (searchParameter.getPageSize() != null) ? Math.min(searchParameter.getPageSize(), maxPageSize) : defaultPageSize;
        if (searchParameter.getMaxResults() != null) {
            pageSize = Math.min(pageSize, searchParameter.getMaxResults());
        }
        return pageSize;
    }

    protected SearchResultDto getElasticsearchResult(InternalSearchContext context) {
        final StopWatch sw = new StopWatch();
        try {
            return getElasticsearchResult(context, sw);
        } finally {
            LOGGER.debug("Performed search on index '{}':\n{}", context.indexPresetConfiguration.getIndexAlias(), new StopWatchPrettyPrint(sw));
        }
    }

    protected SearchResultDto getElasticsearchResult(InternalSearchContext context, StopWatch sw) {
        SearchParameter searchParameter = context.searchParameter;
        if (searchParameter == null) {
            searchParameter = SearchParameter.DEFAULT;
        }

        sw.start("create query");
        final int pageIndex = (searchParameter.getPageIndex() != null) ? searchParameter.getPageIndex() : 1;
        final int start = (pageIndex - 1) * context.pageSize;
        final int maxResults = (searchParameter.getMaxResults() != null)
                ? searchParameter.getMaxResults() : context.indexPresetConfiguration.getMaxResultWindow();
        final int resultLimit = Math.min(context.pageSize, maxResults - start);

        final QueryDto queryDto = createQueryDto(context.accountContext, context.expression, start, resultLimit, searchParameter);
        sw.stop();

        sw.start("process search");
        final SearchResultDto result = elasticsearch.search(queryDto, context.mappingConfiguration(), context.indexPresetConfiguration);
        sw.stop();

        return result;
    }

    protected QueryDto createQueryDto(AccountContext<?> accountContext, Expression expression, int start, int limit, SearchParameter searchParameter) {
        Validate.notNull(accountContext, "Parameter 'accountContext' may not be null!");
        Validate.notNull(expression, "Parameter 'expression' may not be null!");
        Validate.notNull(searchParameter, "Parameter 'searchParameter' may not be null!");

        if (optimizeExpressionsEnabled && searchParameter.isOptimizeExpressions()) {
            expression = expression.optimize();
        }
        final QueryRangeDto queryRangeDto = new QueryRangeDto(start, limit, searchParameter.getMaxTrackTotalHits());
        final List<String> fieldsToResolve = searchParameter.getFieldsToResolve();
        final QueryDto.FieldResolverType fieldResolverType = QueryDto.FieldResolverType.SOURCE_VALUES;
        final Locale locale = StringUtils.isNotBlank(searchParameter.getLanguage())
                ? LocaleUtils.toLocale(searchParameter.getLanguage())
                : accountContext.getUserLanguage();
        return new QueryDto(expression, locale)
                .queryRange(queryRangeDto)
                .sortOptions(searchParameter.getSortOptions())
                .collapseOption(searchParameter.getCollapseOption())
                .aggregations(searchParameter.getAggregations())
                .fieldsToResolve(fieldsToResolve)
                .fieldResolverType(fieldResolverType)
                .accountContext(accountContext);
    }

    protected int getMaxResults(String indexAlias, Integer maxResults, long totalHitCount) {
        if (maxResults == null) {
            final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(indexAlias);
            return (int) Math.min(totalHitCount, indexPresetConfiguration.getMaxResultWindow());
        }
        return (int) Math.min(totalHitCount, (long) maxResults);
    }

    protected List<ResultFacet> convertFacets(List<FacetDto> facets) {
        return facets.stream().map(this::convertFacet).collect(Collectors.toList());
    }

    protected ResultFacet convertFacet(FacetDto facetDto) {
        final List<ResultFacetItem> facetItems = facetDto.getFacetEntryDtos().stream().map(this::convertFacetItem).collect(Collectors.toList());
        return new ResultFacet(facetDto.getName(), facetDto.getFieldName(), facetDto.getCount(), facetItems);
    }

    protected ResultFacetItem convertFacetItem(FacetEntryDto entryDto) {
        return entryDto instanceof RangeFacetItem ? new ResultRangeFacetItem((RangeFacetItem) entryDto) : new ResultFacetItem(entryDto);
    }

    protected MappingConfiguration getMappingConfiguration(String indexAlias, boolean addFieldConfigurations) {
        final List<LanguageSortConfiguration> languageSortConfigurations = new ArrayList<>();
        for (final Locale locale : fieldConfigurationProvider.getSupportedLocales(indexAlias)) {
            languageSortConfigurations.add(new LanguageSortConfiguration(locale));
        }
        final List<? extends FieldConfiguration> fieldConfigurations = addFieldConfigurations
                ? fieldConfigurationProvider.getFieldConfigurations(indexAlias) : null;
        return new MappingConfiguration(
                (fieldConfigurations != null) ? fieldConfigurations : Collections.emptyList(), languageSortConfigurations);
    }

    @Override
    public RestHighLevelClient getRestClient() {
        return elasticsearch.getRestClient();
    }

    protected class InternalSearchContext {

        final IndexPresetConfiguration indexPresetConfiguration;
        final AccountContext<?> accountContext;
        final Expression expression;
        final SearchParameter searchParameter;
        final int pageSize;

        MappingConfiguration mappingConfiguration;

        public InternalSearchContext(IndexPresetConfiguration indexPresetConfiguration, AccountContext<?> accountContext, Expression expression,
                                     SearchParameter searchParameter, int pageSize) {
            this.indexPresetConfiguration = indexPresetConfiguration;
            this.accountContext = accountContext;
            this.expression = expression;
            this.searchParameter = searchParameter;
            this.pageSize = pageSize;
        }

        public InternalSearchContext mappingConfiguration(MappingConfiguration mappingConfiguration) {
            this.mappingConfiguration = mappingConfiguration;
            return this;
        }

        public MappingConfiguration mappingConfiguration() {
            return mappingConfiguration != null ? mappingConfiguration : getMappingConfiguration(indexPresetConfiguration.getIndexAlias(), true);
        }
    }
}
