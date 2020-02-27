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

package de.picturesafe.search.parameter;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Locale;
import java.util.Map;

/**
 * Context of a logged in user
 */
public class AccountContext<I> {

    private static final Locale DEFAULT_LANGUAGE = Locale.GERMAN;

    private I id;
    private String userName;
    private Locale userLanguage = DEFAULT_LANGUAGE;
    private Map<String, Object> attributes;

    /**
     * Default constructor
     */
    public AccountContext() {
    }

    /**
     * Gets the ID of the account.
     * @return ID of the account
     */
    public I getId() {
        return id;
    }

    /**
     * Sets the ID of the account.
     * @param id ID of the account
     */
    public void setId(I id) {
        this.id = id;
    }

    /**
     * Gets the user name.
     * @return User name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     * @param userName User name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets the user language.
     *
     * @return User language
     */
    public Locale getUserLanguage() {
        return userLanguage;
    }

    /**
     * Sets the user language.
     *
     * @param userLanguage User language
     */
    public void setUserLanguage(Locale userLanguage) {
        this.userLanguage = userLanguage;
    }

    /**
     * Gets additional attributes of the account.
     * @return Additional attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets additional attributes of the account.
     * @param attributes Additional attributes
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("id", id) //--
                .append("userName", userName) //--
                .append("userLanguage", userLanguage) //--
                .append("attributes", attributes) //--
                .toString();
    }
}
