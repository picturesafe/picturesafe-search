package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.IndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.model.ElasticsearchInfo;
import de.picturesafe.search.elasticsearch.model.IndexObject;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.SuggestExpression;
import de.picturesafe.search.parameter.AccountContext;
import de.picturesafe.search.parameter.SearchParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("unused")
public class SingleIndexElasticsearchServiceImpl implements SingleIndexElasticsearchService {

    protected ElasticsearchService elasticsearchService;
    protected IndexPresetConfigurationProvider indexPresetConfigurationProvider;

    @Value("${elasticsearch.index.alias:default}")
    protected String defaultAlias;

    @Autowired
    public SingleIndexElasticsearchServiceImpl(ElasticsearchService elasticsearchService,
                                               IndexPresetConfigurationProvider indexPresetConfigurationProvider) {
        this.elasticsearchService = elasticsearchService;
        this.indexPresetConfigurationProvider = indexPresetConfigurationProvider;
    }

    @Autowired(required = false)
    public void setDefaultAlias(String defaultAlias) {
        this.defaultAlias = defaultAlias;
    }

    public ElasticsearchService getElasticsearchService() {
        return elasticsearchService;
    }

    @Override
    public ElasticsearchInfo getElasticsearchInfo() {
        return elasticsearchService.getElasticsearchInfo();
    }

    @Override
    public String getIndexAlias() {
        final IndexPresetConfiguration indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(defaultAlias);
        if (indexPresetConfiguration == null) {
            throw new RuntimeException("No IndexPresetConfiguration found for index alias '" + defaultAlias + "'!");
        }
        return indexPresetConfiguration.getIndexAlias();
    }

    @Override
    public String getIndexName() {
        return elasticsearchService.resolveIndexNames(getIndexAlias()).get(0);
    }

    @Override
    public String createIndexWithAlias() {
        return elasticsearchService.createIndexWithAlias(getIndexAlias());
    }

    @Override
    public void deleteIndexWithAlias() {
        elasticsearchService.deleteIndexWithAlias(getIndexAlias());
    }

    @Override
    public void setIndexVersion(int indexVersion) {
        elasticsearchService.setIndexVersion(getIndexAlias(), indexVersion);
    }

    @Override
    public int getIndexVersion() {
        return elasticsearchService.getIndexVersion(getIndexAlias());
    }

    @Override
    public void addFieldConfiguration(FieldConfiguration... fieldConfigs) {
        elasticsearchService.addFieldConfiguration(getIndexAlias(), fieldConfigs);
    }

    @Override
    public void addToIndex(DataChangeProcessingMode dataChangeProcessingMode, Map<String, Object> document) {
        elasticsearchService.addToIndex(getIndexAlias(), dataChangeProcessingMode, document);
    }

    @Override
    public void addObjectToIndex(DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object) {
        elasticsearchService.addObjectToIndex(getIndexAlias(), dataChangeProcessingMode, object);
    }

    @Override
    public void addObjectToIndex(DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object, long id) {
        elasticsearchService.addObjectToIndex(getIndexAlias(), dataChangeProcessingMode, object, id);
    }

    @Override
    public void addToIndex(DataChangeProcessingMode dataChangeProcessingMode, List<Map<String, Object>> documents) {
        elasticsearchService.addToIndex(getIndexAlias(), dataChangeProcessingMode, documents);
    }

    @Override
    public void addObjectsToIndex(DataChangeProcessingMode dataChangeProcessingMode, List<IndexObject<?>> objects) {
        elasticsearchService.addObjectsToIndex(getIndexAlias(), dataChangeProcessingMode, objects);
    }

    @Override
    public void removeFromIndex(DataChangeProcessingMode dataChangeProcessingMode, long id) {
        elasticsearchService.removeFromIndex(getIndexAlias(), dataChangeProcessingMode, id);
    }

    @Override
    public void removeFromIndex(DataChangeProcessingMode dataChangeProcessingMode, Collection<Long> ids) {
        elasticsearchService.removeFromIndex(getIndexAlias(), dataChangeProcessingMode, ids);
    }

    @Override
    public SearchResult search(Expression expression, SearchParameter searchParameter) {
        return elasticsearchService.search(getIndexAlias(), expression, searchParameter);
    }

    @Override
    public SearchResult search(AccountContext<?> accountContext, Expression expression, SearchParameter searchParameter) {
        return elasticsearchService.search(getIndexAlias(), accountContext, expression, searchParameter);
    }

    @Override
    public Map<String, Object> getDocument(long id) {
        return elasticsearchService.getDocument(getIndexAlias(), id);
    }

    @Override
    public <T extends IndexObject<T>> T getObject(long id, Class<T> type) {
        return elasticsearchService.getObject(getIndexAlias(), id, type);
    }

    @Override
    public SuggestResult suggest(SuggestExpression... expressions) {
        return elasticsearchService.suggest(getIndexAlias(), expressions);
    }
}
