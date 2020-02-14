/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.valuepreparation;

import de.picturesafe.search.elasticsearch.connect.util.PhraseMatchHelper;

public class KeywordValuePreparer implements ValuePreparer {

    @Override
    public void prepare(ValuePrepareContext valuePrepareContext) {
        final Object value = valuePrepareContext.getValue();
        if (value instanceof String) {
            valuePrepareContext.setValue(PhraseMatchHelper.escapePhraseMatchChars((String) value));
        }
    }
}
