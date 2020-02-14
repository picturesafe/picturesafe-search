/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util.logging;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;

public class XcontentToString {

    private final XContentBuilder xContentBuilder;

    public XcontentToString(XContentBuilder xContentBuilder) {
        this.xContentBuilder = xContentBuilder;
    }

    @Override
    public String toString() {
        return Strings.toString(xContentBuilder);
    }
}
