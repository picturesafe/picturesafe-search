/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
