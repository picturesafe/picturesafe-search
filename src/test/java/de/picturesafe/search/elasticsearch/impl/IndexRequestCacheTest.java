/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.impl;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IndexRequestCacheTest {

    @Mock
    private ElasticsearchService elasticsearchService;

    @Test
    public void testStart() {
        final IndexRequestCache cache = new IndexRequestCache();
        assertEquals(0, cache.size("test"));

        cache.put("test", IndexRequest.add(doc(1)));
        assertEquals(0, cache.size("test"));

        cache.start("test");
        cache.put("test", IndexRequest.add(doc(2)));
        assertEquals(1, cache.size("test"));
    }

    @Test
    public void testStop() {
        final IndexRequestCache cache = new IndexRequestCache();
        cache.start("test");
        cache.put("test", IndexRequest.add(doc(1)));
        assertEquals(1, cache.size("test"));

        cache.stop("test");
        cache.put("test", IndexRequest.add(doc(2)));
        assertEquals(1, cache.size("test"));
    }

    @Test
    public void testReplay() {
        final IndexRequestCache cache = new IndexRequestCache();
        cache.start("test");
        cache.put("test", IndexRequest.add(doc(1)));
        cache.put("test", IndexRequest.add(doc(2)));
        cache.put("test", IndexRequest.remove(1L));
        assertEquals(3, cache.size("test"));

        cache.stop("test");
        cache.replay(elasticsearchService, "test");
        verify(elasticsearchService).addToIndex("test", DataChangeProcessingMode.BACKGROUND, Collections.singletonList(doc(1)));
        verify(elasticsearchService).addToIndex("test", DataChangeProcessingMode.BACKGROUND, Collections.singletonList(doc(2)));
        verify(elasticsearchService).removeFromIndex("test", DataChangeProcessingMode.BACKGROUND, Collections.singletonList(1L));
    }

    @Test(expected = IllegalStateException.class)
    public void testReplayErrorNotStopped() {
        final IndexRequestCache cache = new IndexRequestCache();
        cache.start("test");
        cache.put("test", IndexRequest.add(doc(1)));
        cache.replay(elasticsearchService, "test");
    }

    @Test
    public void testClear() {
        final IndexRequestCache cache = new IndexRequestCache();
        cache.start("test");
        cache.put("test", IndexRequest.add(doc(1)));
        assertEquals(1, cache.size("test"));

        cache.stop("test");
        cache.clear("test");
        assertEquals(0, cache.size("test"));
    }

    @Test
    public void testMultipleIndexes() {
        final IndexRequestCache cache = new IndexRequestCache();
        cache.start("test_1");
        cache.start("test_2");
        cache.put("test_1", IndexRequest.add(doc(1)));
        assertEquals(1, cache.size("test_1"));
        assertEquals(0, cache.size("test_2"));

        cache.stop("test_1");
        cache.start("test_2");
        cache.put("test_1", IndexRequest.add(doc(2)));
        cache.put("test_2", IndexRequest.add(doc(1)));
        assertEquals(1, cache.size("test_1"));
        assertEquals(1, cache.size("test_2"));

        cache.clear("test_1");
        cache.put("test_2", IndexRequest.add(doc(1)));
        assertEquals(0, cache.size("test_1"));
        assertEquals(2, cache.size("test_2"));
    }

    private Map<String, Object> doc(long id) {
        final Map<String, Object> doc = new HashMap<>();
        doc.put("id", id);
        return doc;
    }
}
