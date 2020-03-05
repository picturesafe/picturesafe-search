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

package de.picturesafe.search.elasticsearch.model;

import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Item of an elasticsearch result
 */
public class SearchResultItem {

    private final long id;
    private final Map<String, Object> attributes;

    /**
     * Constructor
     *
     * @param attributes            Attributes of the result item's document
     */
    public SearchResultItem(Map<String, Object> attributes) {
        this.id = extractId(attributes);
        this.attributes = attributes;
    }

    private long extractId(Map<String, Object> attributes) {
        final Long id = ElasticDocumentUtils.getId(attributes);
        return (id != null) ? id : -1;
    }

    /**
     * Gets the ID of the result items's document.
     *
     * @return ID
     */
    public long getId() {
        return id;
    }

    /**
     * Gets attributes of the result item's document.
     *
     * @return Attributes of the result item's document
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets a specific attribute.
     *
     * @param name Name of the attribute
     * @return Attribute value
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * Gets a specific date attribute.
     *
     * @param name  Name of the attribute
     * @return      Attribute date value
     */
    public Date getDateAttribute(String name) {
        final Object val = attributes.get(name);
        if (val == null) {
            return null;
        } else if (val instanceof Date) {
            return (Date) val;
        } else if (val instanceof String) {
            return ElasticDateUtils.parseIso((String) val);
        } else {
            throw new IllegalArgumentException("Attribute '" + name + "' is not a date: " + val);
        }
    }

    /**
     * Gets a specific language attribute.
     *
     * @param name      Name of the attribute
     * @param locale    Language of the attribute
     * @return          Attribute language value
     */
    public String getLanguageAttribute(String name, Locale locale) {
        final String key = name + "." + locale.getLanguage();
        final Object val = attributes.get(key);
        if (val == null) {
            return null;
        } else if (val instanceof String) {
            return (String) val;
        } else {
            throw new IllegalArgumentException("Attribute '" + key + "' is not a string: " + val);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("id", id) //--
                .append("attributes", attributes) //--
                .toString();
    }
}
