/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.impl;

import de.picturesafe.search.util.logging.StopWatchPrettyPrint;

import org.slf4j.Logger;
import org.springframework.util.StopWatch;

public abstract class WatchedTask<T> {
    private final T result;

    public WatchedTask(Logger logger, String name) {
        final StopWatch sw = new StopWatch(name);
        sw.start();
        result = process();
        sw.stop();
        logger.debug("{}", new StopWatchPrettyPrint(sw));
    }

    public abstract T process();

    public final T getResult() {
        return result;
    }
}
