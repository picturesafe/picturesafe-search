package de.picturesafe.search.expression;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;

public class FulltextExpression extends ValueExpression {

    public FulltextExpression(String text) {
        super(FieldConfiguration.FIELD_NAME_FULLTEXT, text);
    }
}
