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
