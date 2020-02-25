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

package de.picturesafe.search.elasticsearch.connect.support;

import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.connect.error.IndexMissingException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IndexSetup {

    private static final Logger LOG = LoggerFactory.getLogger(IndexSetup.class);

    private final MappingConfiguration mappingConfiguration;
    private final IndexPresetConfiguration indexPresetConfiguration;
    private final ElasticsearchAdmin elasticsearchAdmin;

    public IndexSetup(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration, ElasticsearchAdmin elasticsearchAdmin) {
        this.mappingConfiguration = mappingConfiguration;
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.elasticsearchAdmin = elasticsearchAdmin;
    }

    public void createIndex(String indexAlias) {
        createIndex(elasticsearchAdmin, indexAlias);
    }

    public void createIndex(ElasticsearchAdmin elasticsearchAdmin, String indexAlias) {
        LOG.info("Creating index: indexAlias = " + indexAlias);
        elasticsearchAdmin.createIndexWithAlias(new StandardIndexPresetConfiguration(indexPresetConfiguration, indexAlias), mappingConfiguration);
    }

    public void tearDownIndex(String indexAlias) {
        try {
            if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
                LOG.info("Deleting index: indexAlias = " + indexAlias);
                elasticsearchAdmin.deleteIndexesOfAlias(indexAlias);
            }
        } catch (IndexMissingException e) {
            LOG.warn("Index or alias does not exist, perhaps deletion was delayed: indexAlias = " + indexAlias);
        }

        if (elasticsearchAdmin.aliasOrIndexExists(indexAlias)) {
            throw new IllegalStateException("Failed to delete index: indexAlias = " + indexAlias);
        }
    }

    public void dumpIndexData(RestHighLevelClient client, String indexAlias) throws Exception {
        final List<String> indexNames = elasticsearchAdmin.resolveIndexNames(indexAlias);

        final StringBuilder sb = new StringBuilder();
        sb.append("The elasticsearch alias '").append(indexAlias).append("' consists of '").append(indexNames.size()).append("' indices.");
        for (String indexName : indexNames) {
            sb.append("\r\n  ").append(indexName).append(" := ");
            sb.append("\r\n    Configuration:\r\n").append(elasticsearchAdmin.getMappingAsJson(indexName));
            sb.append("\r\n    Content:\r\n");

            final SearchRequest searchRequest = new SearchRequest(indexAlias);
            final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);
            final SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit searchHit : response.getHits().getHits()) {
                sb.append(searchHit.getSourceAsString());
            }
        }
        LOG.debug(sb.toString());
    }
}
