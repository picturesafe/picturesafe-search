/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util.logging;

import org.springframework.util.StopWatch;

public class StopWatchPrettyPrint {

    private final StopWatch stopWatch;

    public StopWatchPrettyPrint(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public String toString() {
        return (stopWatch != null) ? stopWatch.prettyPrint() : null;
    }
}
