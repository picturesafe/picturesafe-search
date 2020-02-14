/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.config;

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Locale;

/**
 * Configuration for language specific sort orders.
 *
 * See https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-icu-collation-keyword-field.html
 */
public class LanguageSortConfiguration {

    private final Locale locale;
    private final String variant;

    /**
     * Constructor
     * @param locale The language locale
     */
    public LanguageSortConfiguration(Locale locale) {
        this(locale, null);
    }

    /**
     * Constructor
     * @param locale The language locale
     * @param variant ICU collation variant (e.g. "phonebook")
     */
    public LanguageSortConfiguration(Locale locale, String variant) {
        this.locale = locale;
        this.variant = variant;
    }

    /**
     * Gets the language locale
     * @return Language locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Gets the ISO 639-1 language code.
     * @return ISO 639-1 language code
     */
    public String getLanguage() {
        return locale.getLanguage();
    }

    /**
     * Gets the ISO 3166-1 ALPHA-2 country code.
     * @return ISO 3166-1 ALPHA-2 country code
     */
    public String getCountry() {
        return (StringUtils.isNotEmpty(locale.getCountry())) ? locale.getCountry() : locale.getLanguage().toUpperCase(Locale.ROOT);
    }

    /**
     * Gets the ICU collation variant (e.g. "phonebook").
     * @return ICU collation variant or <code>null</code>
     */
    public String getVariant() {
        return variant;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("locale", locale) //--
                .append("variant", variant) //--
                .toString();
    }
}
