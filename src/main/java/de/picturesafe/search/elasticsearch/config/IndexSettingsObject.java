/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import java.io.IOException;

/**
 * Configuration of a custom index settings object.
 */
public class IndexSettingsObject {

    private final String name;
    private String json;
    private XContentBuilder content;

    /**
     * Constructor
     * @param name Name of the index settings object
     */
    public IndexSettingsObject(String name) {
        this.name = name;
        try {
            this.content = JsonXContent.contentBuilder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor
     * @param name Name of the index settings object
     * @param json Settings of the object as JSON
     */
    public IndexSettingsObject(String name, String json) {
        Validate.notEmpty(name, "Parameter 'name' may not be null or empty!");
        Validate.notEmpty(json, "Parameter 'json' may not be null or empty!");

        this.name = name;
        this.json = json;
    }

    /**
     * Gets the name of the index settings object.
     * @return Name of the index settings object
     */
    public String name() {
        return name;
    }

    /**
     * Gets the settings of the object as JSON.
     * @return Settings of the object as JSON
     */
    public String json() {
        return (json != null) ? json : Strings.toString(content);
    }

    /**
     * Gets the settings of the object as XContentBuilder.
     * @return Settings of the object as XContentBuilder
     */
    public XContentBuilder content() {
        return content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("name", name) //--
                .append("json", json) //--
                .append("content", content) //--
                .toString();
    }
}
