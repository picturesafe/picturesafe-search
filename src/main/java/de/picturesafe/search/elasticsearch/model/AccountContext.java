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

import java.util.Locale;

/**
 * Context of a logged in user
 */
public class AccountContext {

    private static final Locale DEFAULT_LANGUAGE = Locale.GERMAN;

    private Locale currentLoginLanguage = DEFAULT_LANGUAGE;

    /**
     * Default constructor
     */
    public AccountContext() {
    }

    /**
     * Constructor
     *
     * @param currentLoginLanguage Current login language
     */
    public AccountContext(Locale currentLoginLanguage) {
        this.currentLoginLanguage = currentLoginLanguage;
    }

    /**
     * Gets the current login language.
     *
     * @return Current login language
     */
    public Locale getCurrentLoginLanguage() {
        return currentLoginLanguage;
    }

    /**
     * Sets the current login language.
     *
     * @param currentLoginLanguage Current login language
     */
    public void setCurrentLoginLanguage(Locale currentLoginLanguage) {
        this.currentLoginLanguage = currentLoginLanguage;
    }
}
