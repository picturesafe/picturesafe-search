/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * Suggest expression for search-as-you-type functionality.
 */
public class SuggestExpression extends AbstractExpression implements FieldExpression {

    private final String text;
    private final int count;

    /**
     * Constructor
     *
     * @param text Text typed in to find suggestions for
     * @param count Number of suggestions to return
     */
    public SuggestExpression(String text, int count) {
        this.text = text;
        this.count = count;
    }

    @Override
    public String getName() {
        return FieldConfiguration.FIELD_NAME_SUGGEST;
    }

    /**
     * Gets the text to find suggestions for.
     * @return Text to find suggestions for
     */
    public String getText() {
        return text;
    }

    /**
     * Getsthe number of suggestions to return.
     * @return Number of suggestions to return
     */
    public int getCount() {
        return count;
    }

    @Override
    public Expression optimize() {
        return StringUtils.isNotBlank(text) ? this : null;
    }
}
