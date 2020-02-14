/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.asyncaction;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes an asynchronous residual client action and waits for the result.
 * An instance can only be used once.
 * The asynchronous execution of requests increases scalability, because threads are not blocked.
 */
public abstract class AbstractRestClientAsyncAction<Request, Response> implements ActionListener<Response> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRestClientAsyncAction.class);
    private Exception exception;
    private Response response;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private AtomicBoolean used = new AtomicBoolean(false);

    abstract void asyncAction(RestHighLevelClient client, Request request);

    public Response action(RestHighLevelClient client, Request request) {
        if (used.get()) {
            throw new RuntimeException("Action already triggered once and can not be used again. Please create a new Action");
        }
        used.set(true);

        boolean searchDone = false;
        asyncAction(client, request);
        try {
            searchDone = countDownLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOG.error("Failed to wait for the search result", e);
        }

        if (response == null) {
            if (searchDone) {
                throw new RuntimeException("Action failed! " + request, exception);
            } else {
                throw new RuntimeException("Action failed due to timeout! " + request);
            }
        }

        return response;
    }

    @Override
    public void onFailure(Exception e) {
        this.exception = e;
        countDownLatch.countDown();
    }

    @Override
    public void onResponse(Response response) {
        this.response = response;
        countDownLatch.countDown();
    }
}