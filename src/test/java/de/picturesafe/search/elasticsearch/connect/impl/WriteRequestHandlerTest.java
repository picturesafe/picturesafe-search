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

import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.index.shard.ShardId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WriteRequestHandlerTest {

    @Mock
    private RestClientConfiguration restClientConfiguration;

    @Mock
    private WriteRequestHandler requestHandler;

    private ElasticsearchImpl elasticsearch;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        final ElasticsearchAdmin elasticsearchAdmin = mock(ElasticsearchAdmin.class);
        elasticsearch
                = spy(new ElasticsearchImpl(elasticsearchAdmin, restClientConfiguration, Collections.emptyList(), Collections.emptyList(), "Europe/Berlin"));
        elasticsearch.setWriteRequestHandler(requestHandler);
        elasticsearch.indexingBulkSize = 100;
        doAnswer(invocation -> {
            final WriteRequest<?> request = (WriteRequest<?>) invocation.getArguments()[0];
            if (request instanceof IndexRequest) {
                return indexResponse((IndexRequest) request);
            } else if (request instanceof DeleteRequest) {
                return deleteResponse((DeleteRequest) request);
            } else if (request instanceof BulkRequest) {
                final BulkItemResponse[] responses = ((BulkRequest) request).requests().stream().map(this::bulkItemResponse).toArray(BulkItemResponse[]::new);
                return new BulkResponse(responses, 0);
            } else {
                throw new RuntimeException("Unsupported request type: " + request.getClass().getName());
            }
        }).when(elasticsearch).handleRequest(any(WriteRequest.class));
    }

    private IndexResponse indexResponse(IndexRequest request) {
        return new IndexResponse(shardId(), "test", request.id(), 0, 0, 0, true);
    }

    private DeleteResponse deleteResponse(DeleteRequest request) {
        return new DeleteResponse(shardId(), "test", request.id(), 0, 0, 0, true);
    }

    private ShardId shardId() {
        return new ShardId("test", "uuid", 1);
    }

    private BulkItemResponse bulkItemResponse(DocWriteRequest<?> request) {
        return new BulkItemResponse(0, (request instanceof IndexRequest ? DocWriteRequest.OpType.INDEX : DocWriteRequest.OpType.DELETE),
                (request instanceof IndexRequest ? indexResponse((IndexRequest) request) : deleteResponse((DeleteRequest) request)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddSingle() {
        elasticsearch.addToIndex("test", false, DocumentBuilder.id(1).build());
        verify(requestHandler, times(1)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));

        doReturn(true).when(requestHandler).handle(any(WriteRequest.class));
        elasticsearch.removeFromIndex("test", false, 1);
        verify(requestHandler, times(2)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddBulk() {
        final List<Map<String, Object>> docs = Arrays.asList(DocumentBuilder.id(1).build(), DocumentBuilder.id(2).build());
        elasticsearch.addToIndex("test", false, true, docs);
        verify(requestHandler, times(1)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));

        doReturn(true).when(requestHandler).handle(any(WriteRequest.class));
        elasticsearch.addToIndex("test", false, true, docs);
        verify(requestHandler, times(2)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveSingle() {
        elasticsearch.removeFromIndex("test", false, 1);
        verify(requestHandler, times(1)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));

        doReturn(true).when(requestHandler).handle(any(WriteRequest.class));
        elasticsearch.removeFromIndex("test", false, 1);
        verify(requestHandler, times(2)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRemoveBulk() {
        final List<Integer> ids = Arrays.asList(1, 2);
        elasticsearch.removeFromIndex("test", false, ids);
        verify(requestHandler, times(1)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));

        doReturn(true).when(requestHandler).handle(any(WriteRequest.class));
        elasticsearch.removeFromIndex("test", false, ids);
        verify(requestHandler, times(2)).handle(any(IndexRequest.class));
        verify(elasticsearch, times(1)).handleRequest(any(WriteRequest.class));
    }
}
