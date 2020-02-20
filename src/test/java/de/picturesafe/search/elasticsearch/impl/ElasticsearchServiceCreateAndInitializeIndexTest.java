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
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.IndexInitializationListener;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchServiceCreateAndInitializeIndexTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceCreateAndInitializeIndexTest.class);

    @Mock
    private Elasticsearch elasticsearch;

    @Mock
    private FieldConfigurationProvider fieldConfigurationProvider;

    @Mock
    private DocumentProvider documentProvider;

    @Test
    public void testSequentialAsync() throws Exception {
        final String indexAlias = "test";
        final int threadCount = 3;
        final ElasticsearchService elasticsearchService = elasticsearchService();

        final List<IndexInitializationListener> listeners = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final IndexInitializationListener listener = spy(new TestListener(indexAlias, latch));
            listeners.add(listener);
            elasticsearchService.createAndInitializeIndex(indexAlias, true, listener, DataChangeProcessingMode.BACKGROUND);
            LOGGER.debug("[{}] Initialization of '{}' submitted", i, indexAlias);
        }

        latch.await(30, TimeUnit.SECONDS);
        final InOrder callOrder = inOrder(listeners.toArray());
        for (final IndexInitializationListener listener : listeners) {
            callOrder.verify(listener).updateProgress(any(IndexInitializationListener.Event.class));
        }
    }

    @Test
    public void testSequentialSync() {
        final String indexAlias = "test";
        final ElasticsearchService elasticsearchService = elasticsearchService();

        final List<IndexInitializationListener> listeners = new ArrayList<>();
        final int i = 0;
        final IndexInitializationListener listener = spy(new TestListener(indexAlias, null));
        listeners.add(listener);
        elasticsearchService.createAndInitializeIndex(indexAlias, true, listener, DataChangeProcessingMode.BLOCKING);
        LOGGER.debug("[{}] Initialization of '{}' submitted", i, indexAlias);

        final InOrder callOrder = inOrder(listeners.toArray());
        for (final IndexInitializationListener l : listeners) {
            callOrder.verify(l).updateProgress(any(IndexInitializationListener.Event.class));
        }
    }

    @Test
    public void testParallel() throws Exception {
        final int threadCount = 20;
        final ElasticsearchService elasticsearchService = elasticsearchService();
        final CountDownLatch latch = new CountDownLatch(threadCount);

        final List<TestListener> listeners = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final String indexAlias = "test-" + (i / 3);
            final TestListener listener = spy(new TestListener(indexAlias, latch));
            listeners.add(listener);
            elasticsearchService.createAndInitializeIndex(indexAlias, true, listener, DataChangeProcessingMode.BACKGROUND);
            LOGGER.debug("[{}] Initialization of '{}' submitted", i, indexAlias);
        }

        latch.await(30, TimeUnit.SECONDS);

        for (TestListener listener : listeners) {
            verify(listener).updateProgress(any(IndexInitializationListener.Event.class));
        }

        String collect = listeners.stream()
                .filter(listener -> !listener.threadName.contains(listener.indexAlias))
                .map(listener -> "Thread " + listener.threadName + " processed alias " + listener.indexAlias)
                .collect(Collectors.joining("\n"));

        Assert.assertEquals("There must be no listeners that are processed by wrong thread", "", collect);
    }

    private ElasticsearchService elasticsearchService() {
        final List<IndexPresetConfiguration> indexPresetConfigurations = new ArrayList<>();
        indexPresetConfigurations.add(new StandardIndexPresetConfiguration("test", 1, 0));
        final ElasticsearchServiceImpl elasticsearchService = new ElasticsearchServiceImpl(elasticsearch, indexPresetConfigurations, fieldConfigurationProvider);
        elasticsearchService.setDocumentProvider(documentProvider);

        final ElasticsearchServiceImpl elasticsearchServiceSpy = spy(elasticsearchService);
        doAnswer(invocation -> {
            final IndexInitializer indexInitializer = mock(IndexInitializer.class);
            doAnswer(i -> {
                IndexInitializationListener listener = i.getArgumentAt(1, IndexInitializationListener.class);
                String alias = i.getArgumentAt(0, String.class);
                listener.updateProgress(new IndexInitializationListener.Event(alias, IndexInitializationListener.Event.Type.END, 0, 0));
                return null;
            }).when(indexInitializer).init(anyString(), any(IndexInitializationListener.class));
            return indexInitializer;
        }).when(elasticsearchServiceSpy).indexInitializer();

        return elasticsearchServiceSpy;
    }

    private static class TestListener implements IndexInitializationListener {

        final String indexAlias;
        private final CountDownLatch latch;
        String threadName;

        TestListener(String indexAlias, CountDownLatch latch) {
            this.indexAlias = indexAlias;
            this.latch = latch;
        }

        @Override
        public void updateProgress(Event event) {
            if (event.getType() == IndexInitializationListener.Event.Type.END) {
                assert threadName == null : "Thread is already set:" + threadName + " for index " + indexAlias;
                threadName = Thread.currentThread().getName();
                if (latch != null) {
                    latch.countDown();
                }
            }
        }
    }
}
