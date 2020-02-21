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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.fail;

public class IndexSetup {

    private static final Logger LOG = LoggerFactory.getLogger(IndexSetup.class);
    private static final String STANDARD_DATE_FORMAT = "dd.MM.yyyy";

    private final MappingConfiguration mappingConfiguration;
    private final IndexPresetConfiguration indexPresetConfiguration;
    private final ElasticsearchAdmin elasticsearchAdmin;

    public IndexSetup(MappingConfiguration mappingConfiguration, IndexPresetConfiguration indexPresetConfiguration, ElasticsearchAdmin elasticsearchAdmin) {
        this.mappingConfiguration = mappingConfiguration;
        this.indexPresetConfiguration = indexPresetConfiguration;
        this.elasticsearchAdmin = elasticsearchAdmin;
    }

    public void setupIndex(String indexAlias) throws Exception {
        createIndex(indexAlias);

        final BulkRequest request = new BulkRequest();
        try {
            final int[] allowedCountryIds = new int[]{1};
            final int[] lockedCountryIds = new int[]{2};
            final Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
            final Date thatday = DateUtils.parseDate("01.01.1923", STANDARD_DATE_FORMAT); // Problematisches Datum: 01.01.1923
            LOG.debug("thatday=" + thatday);
            request.add(createIndexRequest(indexAlias, 1, null, "erster wert 1",
                    "caption1", "1", new String[]{"system1", "system2"}, null, null,
                    new int[]{4711}, "Hamburg Altona"));
            request.add(createIndexRequest(indexAlias, 2, null, "zweiter wert 2",
                    "caption2", "2", new String[]{}, null, lockedCountryIds,
                    new int[]{4711}, "Bremen"));
            request.add(createIndexRequest(indexAlias, 3, null, "dritter wert 3",
                    "caption2", "3", new String[]{}, allowedCountryIds, null,
                    new int[]{}, "Rostock"));
            request.add(createIndexRequest(indexAlias, 4, thatday, "vierter wert 4",
                    "Schleswig-Holstein liegt im Norden", "4", new String[]{}, allowedCountryIds, lockedCountryIds,
                    new int[]{4711, 4712}, "Bosnien Herzegowina"));
            request.add(createIndexRequest(indexAlias, 5, today, "fünfter wert 5",
                    "Schleswig liegt nicht in Holstein", "5", new String[]{}, allowedCountryIds, lockedCountryIds,
                    new int[]{4712}, null));
            request.add(createIndexRequest(indexAlias, 6, createTestDocument(6, null, "Released",
                    "Record released", null, null, null, null,
                    new int[]{4712}, null, true)));
            request.add(createIndexRequest(indexAlias, 7, createTestDocument(7, null, "Not released",
                    "Record not released", null, null, null, null,
                    new int[]{4712}, null, false)));

        } catch (IOException e) {
            throw new RuntimeException("Could not create Index data", e);
        }
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (BulkItemResponse bulkItemResponse : elasticsearchAdmin.getRestClient().bulk(request, RequestOptions.DEFAULT).getItems()) {
            if (bulkItemResponse.isFailed()) {
                LOG.error(bulkItemResponse.getFailureMessage(), bulkItemResponse.getFailure().getCause());
                fail("Index setup failed: " + bulkItemResponse.getFailureMessage());
            } else {
                LOG.debug("Inserted item with id " + bulkItemResponse.getId());
            }
        }
    }

    public void createIndex(String indexAlias) {
        createIndex(elasticsearchAdmin, indexAlias);
    }

    public void createIndex(ElasticsearchAdmin elasticsearchAdmin, String indexAlias) {
        LOG.info("Creating index: indexAlias = " + indexAlias);
        elasticsearchAdmin.createIndexWithAlias(new StandardIndexPresetConfiguration(indexPresetConfiguration, indexAlias), mappingConfiguration);
    }

    private IndexRequest createIndexRequest(String indexAlias, int id, Date createDate, String title, String caption, String facetResolved,
                                            String[] systemField, int[] allowedCountryIds, int[] lockedCountryIds, int[] roleRightIds, String location) throws IOException {

        return createIndexRequest(indexAlias, id, createTestDocument(id, createDate, title, caption, facetResolved, systemField,
                allowedCountryIds, lockedCountryIds, roleRightIds, location));
    }

    private IndexRequest createIndexRequest(String indexAlias, int id, XContentBuilder doc) {
        return new IndexRequest(indexAlias).id(Integer.toString(id)).source(doc);
    }

    private XContentBuilder createTestDocument(int id, Date createDate, String title, String caption, String facetResolved, String[] systemField,
                                           int[] allowedCountryIds, int[] lockedCountryIds, int[] roleRightIds, String location) throws IOException {
        return createTestDocument(id, createDate, title, caption, facetResolved, systemField, allowedCountryIds, lockedCountryIds, roleRightIds, location, null);
    }

    private XContentBuilder createTestDocument(int id, Date createDate, String title, String caption, String facetResolved, String[] systemField,
                                           int[] allowedCountryIds, int[] lockedCountryIds, int[] roleRightIds, String location, Boolean released) throws IOException {
        final XContentBuilder xContentBuilder = jsonBuilder().startObject();
        xContentBuilder.field("title.de", title);
        xContentBuilder.field("id", id);
        xContentBuilder.field("caption", caption);
        if (StringUtils.isNotEmpty(facetResolved)) {
            xContentBuilder.field("facetResolved", facetResolved);
        }
        if (ArrayUtils.isNotEmpty(systemField)) {
            xContentBuilder.field("systemField", systemField);
        }
        if (createDate != null) {
            xContentBuilder.field("createDate", createDate);
        }
        if (allowedCountryIds != null && allowedCountryIds.length > 0) {
            xContentBuilder.field("allowedCountryIds", allowedCountryIds);
        }
        if (lockedCountryIds != null && lockedCountryIds.length > 0) {
            xContentBuilder.field("lockedCountryIds", lockedCountryIds);
        }
        if (roleRightIds != null && roleRightIds.length > 0) {
            xContentBuilder.field("roleRightIds", roleRightIds);
        }
        if (!StringUtils.isEmpty(location)) {
            xContentBuilder.field("location", location);
        }
        if (released != null) {
            xContentBuilder.field("released", released);
        }

        xContentBuilder.endObject();
        return xContentBuilder;
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
