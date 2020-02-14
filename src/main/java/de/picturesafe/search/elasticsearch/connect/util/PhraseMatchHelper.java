/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

public class PhraseMatchHelper {

    public static boolean matchPhrase(String value) {
        return value.startsWith("\"") && value.endsWith("\"") || value.startsWith("{") && value.endsWith("}");
    }

    public static String escapePhraseMatchChars(String value) {
        return matchPhrase(value) ? value.substring(1, value.length() - 1) : value;
    }

    public static String replacePhraseMatchChars(String value) {
        if (value.startsWith("{") && value.endsWith("}")) {
            return "\"" + value.substring(1, value.length() - 1) + "\"";
        }

        return value;
    }
}
