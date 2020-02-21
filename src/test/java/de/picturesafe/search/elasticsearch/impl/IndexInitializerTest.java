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
import de.picturesafe.search.elasticsearch.DocumentProvider;
import de.picturesafe.search.elasticsearch.IndexInitializationListener;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getId;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IndexInitializerTest {

    @Mock
    private InternalElasticsearchService elasticsearchService;

    @Test
    @SuppressWarnings("unchecked")
    public void testIndexActions() {
        final String indexAlias = "test";
        final IndexInitializationListener listener = mock(IndexInitializationListener.class);
        final String indexName = indexInitializer(indexAlias, false, true).init(indexAlias, listener);

        final ArgumentCaptor<List> docs = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<List> ids = ArgumentCaptor.forClass(List.class);
        // Index build
        verify(elasticsearchService, times(3)).addToIndex(any(MappingConfiguration.class), eq(indexName),
                eq(DataChangeProcessingMode.BACKGROUND), docs.capture());
        // Delta processing
        verify(elasticsearchService, times(2)).addToIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), docs.capture());
        verify(elasticsearchService, times(2)).removeFromIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), ids.capture());

        final List<List> addedDocs = docs.getAllValues();
        long id = 0;
        for (final List list : addedDocs) {
            for (final Object obj : list) {
                final Map<String, Object> doc = (Map<String, Object>) obj;
                assertEquals(++id, getId(doc));
            }
        }
        assertEquals(12, id);

        final List<List> deletedIs = ids.getAllValues();
        id = 6;
        for (final List list : deletedIs) {
            for (final Object obj : list) {
                final Long deletedId = (Long) obj;
                assertEquals(id++, deletedId.longValue());
            }
        }
    }

    @Test
    public void testCallOrder() {
        final String indexAlias = "test";
        final IndexInitializationListener listener = mock(IndexInitializationListener.class);

        for (final boolean indexExists : asList(false, true)) {
            final String indexName = indexInitializer(indexAlias, indexExists, true).init(indexAlias, listener);

            final InOrder callOrder = inOrder(elasticsearchService);
            // Create index
            callOrder.verify(elasticsearchService).createIndex(eq(indexAlias));
            // Maybe set alias on new index
            if (!indexExists) {
                callOrder.verify(elasticsearchService).createAlias(eq(indexAlias), anyString());
            }
            // Index build
            callOrder.verify(elasticsearchService).addToIndex(any(MappingConfiguration.class), eq(indexName), eq(DataChangeProcessingMode.BACKGROUND),
                    eq(docs(1, 3)));
            callOrder.verify(elasticsearchService).addToIndex(any(MappingConfiguration.class), eq(indexName), eq(DataChangeProcessingMode.BACKGROUND),
                    eq(docs(4, 3)));
            callOrder.verify(elasticsearchService).addToIndex(any(MappingConfiguration.class), eq(indexName), eq(DataChangeProcessingMode.BACKGROUND),
                    eq(docs(7, 3)));
            // Maybe switch alias
            if (indexExists) {
                callOrder.verify(elasticsearchService).removeAlias(eq(indexAlias));
                callOrder.verify(elasticsearchService).createAlias(eq(indexAlias), anyString());
            }
            // Delta processing
            callOrder.verify(elasticsearchService).addToIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), eq(asList(doc(10), doc(11))));
            callOrder.verify(elasticsearchService).removeFromIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), eq(singletonList(6L)));
            callOrder.verify(elasticsearchService).addToIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), eq(singletonList(doc(12))));
            callOrder.verify(elasticsearchService).removeFromIndex(eq(indexAlias), eq(DataChangeProcessingMode.BACKGROUND), eq(asList(7L, 8L)));
            // Maybe delete old index
            if (indexExists) {
                callOrder.verify(elasticsearchService).deleteIndex(eq("test-4711"));
            }
        }
    }

    @Test
    public void testEventsWithoutIndex() {
        final String indexAlias = "test";
        final IndexInitializationListener listener = mock(IndexInitializationListener.class);
        indexInitializer(indexAlias, false, true).init(indexAlias, listener);

        final ArgumentCaptor<IndexInitializationListener.Event> events = ArgumentCaptor.forClass(IndexInitializationListener.Event.class);
        verify(listener, times(7)).updateProgress(events.capture());
        final List<IndexInitializationListener.Event> capturedEvents = events.getAllValues();
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.CREATE_INDEX, 0, 0), capturedEvents.get(0));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.SET_ALIAS, 0, 0), capturedEvents.get(1));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 0, 9), capturedEvents.get(2));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 3, 9), capturedEvents.get(3));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 6, 9), capturedEvents.get(4));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.PROCESS_DELTA, 9, 15), capturedEvents.get(5));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.END, 15, 15), capturedEvents.get(6));
    }

    @Test
    public void testEventsWithoutIndexAndDelta() {
        final String indexAlias = "test";
        final IndexInitializationListener listener = mock(IndexInitializationListener.class);
        indexInitializer(indexAlias, false, false).init(indexAlias, listener);

        final ArgumentCaptor<IndexInitializationListener.Event> events = ArgumentCaptor.forClass(IndexInitializationListener.Event.class);
        verify(listener, times(6)).updateProgress(events.capture());
        final List<IndexInitializationListener.Event> capturedEvents = events.getAllValues();
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.CREATE_INDEX, 0, 0), capturedEvents.get(0));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.SET_ALIAS, 0, 0), capturedEvents.get(1));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 0, 9), capturedEvents.get(2));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 3, 9), capturedEvents.get(3));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 6, 9), capturedEvents.get(4));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.END, 9, 9), capturedEvents.get(5));
    }

    @Test
    public void testEventsWithExistingIndex() {
        final String indexAlias = "test";
        final IndexInitializationListener listener = mock(IndexInitializationListener.class);
        indexInitializer(indexAlias, true, true).init(indexAlias, listener);

        final ArgumentCaptor<IndexInitializationListener.Event> events = ArgumentCaptor.forClass(IndexInitializationListener.Event.class);
        verify(listener, times(8)).updateProgress(events.capture());
        final List<IndexInitializationListener.Event> capturedEvents = events.getAllValues();
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.CREATE_INDEX, 0, 0), capturedEvents.get(0));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 0, 9), capturedEvents.get(1));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 3, 9), capturedEvents.get(2));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.ADD_DOCUMENTS, 6, 9), capturedEvents.get(3));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.SET_ALIAS, 9, 9), capturedEvents.get(4));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.PROCESS_DELTA, 9, 15), capturedEvents.get(5));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.DELETE_OLD_INDEX, 15, 15), capturedEvents.get(6));
        assertEquals(new IndexInitializationListener.Event(indexAlias, IndexInitializationListener.Event.Type.END, 15, 15), capturedEvents.get(7));
    }

    private IndexInitializer indexInitializer(String indexAlias, boolean indexExists, boolean addDelta) {
        final IndexRequestCache indexRequestCache = new IndexRequestCache();
        final IndexInitializer indexInitializer = new IndexInitializer(elasticsearchService, documentProvider(), indexRequestCache);
        final IndexInitializer indexInitializerSpy = spy(indexInitializer);
        if (addDelta) {
            doAnswer((invocation) -> {
                indexRequestCache.start(indexAlias);
                indexRequestCache.put(indexAlias, IndexRequest.add(doc(10), doc(11)));
                indexRequestCache.put(indexAlias, IndexRequest.remove(6L));
                indexRequestCache.put(indexAlias, IndexRequest.add(doc(12)));
                indexRequestCache.put(indexAlias, IndexRequest.remove(7L, 8L));
                final Object[] args = invocation.getArguments();
                return indexInitializer.addDocuments((String) args[0], (String) args[1], (IndexInitializationListener) args[2]);
            }).when(indexInitializerSpy).addDocuments(anyString(), anyString(), any(IndexInitializationListener.class));
        }
        if (indexExists) {
            doReturn(true).when(elasticsearchService).aliasExists(indexAlias);
            doReturn(singletonList("test-4711")).when(indexInitializerSpy).getOldIndexNames(indexAlias);
        }
        return indexInitializerSpy;
    }

    private DocumentProvider documentProvider() {
        return (indexAlias, handler) -> {
            for (int i = 0; i < 3; i++) {
                handler.handleDocuments(new DocumentProvider.DocumentChunk(docs(i * 3 + 1, 3), 3 * i, 3 * 3));
            }
        };
    }

    private List<Map<String, Object>> docs(long startId, int count) {
        final List<Map<String, Object>> docs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            docs.add(doc(startId + i));
        }
        return docs;
    }

    private Map<String, Object> doc(long id) {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        return doc;
    }
}
