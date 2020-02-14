/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IndexSettingsObjectTest {

    @Test
    public void testContentBuilder() throws Exception {
        String json = "" +
                "{" +
                "    \"type\": \"char_group\"," +
                "    \"tokenize_on_chars\": [" +
                "      \"whitespace\"," +
                "      \".\"," +
                "      \"-\"," +
                "      \"_\"," +
                "      \"\\n\"" +
                "    ]" +
                "}";
        json = json.replaceAll("\\s+","");

        final IndexSettingsObject iso = new IndexSettingsObject("file_name_tokenizer");
        iso.content().startObject()
                .field("type", "char_group")
                .array("tokenize_on_chars", "whitespace", ".", "-", "_", "\n")
                .endObject();
        assertEquals(json, iso.json().replaceAll("\\s+",""));
    }
}
