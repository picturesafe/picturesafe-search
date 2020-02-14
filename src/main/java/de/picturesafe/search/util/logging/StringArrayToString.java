/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util.logging;

public class StringArrayToString {

    private final String[] strings;

    public StringArrayToString(String[] strings) {
        this.strings = strings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String str : strings) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("'").append(str).append("'");
        }
        return sb.toString();
    }
}
