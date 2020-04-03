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
    protected IndexPresetConfiguration indexPresetConfiguration;

    @Autowired
    public SingleIndexElasticsearchServiceImpl(ElasticsearchService elasticsearchService,
                                               IndexPresetConfigurationProvider indexPresetConfigurationProvider,
                                               @Value("${elasticsearch.index.alias:default}") String defaultAlias) {
        this.elasticsearchService = elasticsearchService;
        // Compatibility to ElasticsearchServiceImpl
        this.indexPresetConfiguration = indexPresetConfigurationProvider.getIndexPresetConfiguration(defaultAlias);
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
        return indexPresetConfiguration.getIndexAlias();
    }

    @Override
    public String getIndexName() {
        return elasticsearchService.resolveIndexNames(indexPresetConfiguration.getIndexAlias()).get(0);
    }

    @Override
    public String createIndexWithAlias() {
        return elasticsearchService.createIndexWithAlias(indexPresetConfiguration.getIndexAlias());
    }

    @Override
    public void deleteIndexWithAlias() {
        elasticsearchService.deleteIndexWithAlias(indexPresetConfiguration.getIndexAlias());
    }

    @Override
    public void setIndexVersion(int indexVersion) {
        elasticsearchService.setIndexVersion(indexPresetConfiguration.getIndexAlias(), indexVersion);
    }

    @Override
    public int getIndexVersion() {
        return elasticsearchService.getIndexVersion(indexPresetConfiguration.getIndexAlias());
    }

    @Override
    public void addFieldConfiguration(FieldConfiguration... fieldConfigs) {
        elasticsearchService.addFieldConfiguration(indexPresetConfiguration.getIndexAlias(), fieldConfigs);
    }

    @Override
    public void addToIndex(DataChangeProcessingMode dataChangeProcessingMode, Map<String, Object> document) {
        elasticsearchService.addToIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, document);
    }

    @Override
    public void addObjectToIndex(DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object) {
        elasticsearchService.addObjectToIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, object);
    }

    @Override
    public void addObjectToIndex(DataChangeProcessingMode dataChangeProcessingMode, IndexObject<?> object, long id) {
        elasticsearchService.addObjectToIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, object, id);
    }

    @Override
    public void addToIndex(DataChangeProcessingMode dataChangeProcessingMode, List<Map<String, Object>> documents) {
        elasticsearchService.addToIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, documents);
    }

    @Override
    public void addObjectsToIndex(DataChangeProcessingMode dataChangeProcessingMode, List<IndexObject<?>> objects) {
        elasticsearchService.addObjectsToIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, objects);
    }

    @Override
    public void removeFromIndex(DataChangeProcessingMode dataChangeProcessingMode, long id) {
        elasticsearchService.removeFromIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, id);
    }

    @Override
    public void removeFromIndex(DataChangeProcessingMode dataChangeProcessingMode, Collection<Long> ids) {
        elasticsearchService.removeFromIndex(indexPresetConfiguration.getIndexAlias(), dataChangeProcessingMode, ids);
    }

    @Override
    public SearchResult search(Expression expression, SearchParameter searchParameter) {
        return elasticsearchService.search(indexPresetConfiguration.getIndexAlias(), expression, searchParameter);
    }

    @Override
    public SearchResult search(AccountContext<?> accountContext, Expression expression, SearchParameter searchParameter) {
        return elasticsearchService.search(indexPresetConfiguration.getIndexAlias(), accountContext, expression, searchParameter);
    }

    @Override
    public Map<String, Object> getDocument(long id) {
        return elasticsearchService.getDocument(indexPresetConfiguration.getIndexAlias(), id);
    }

    @Override
    public <T extends IndexObject<T>> T getObject(long id, Class<T> type) {
        return elasticsearchService.getObject(indexPresetConfiguration.getIndexAlias(), id, type);
    }

    @Override
    public SuggestResult suggest(SuggestExpression... expressions) {
        return elasticsearchService.suggest(indexPresetConfiguration.getIndexAlias(), expressions);
    }
}
