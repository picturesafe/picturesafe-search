/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

/**
 * Expression to match field values
 */
public interface FieldExpression extends Expression {

    /**
     * Gets the field name
     * @return Field name
     */
    String getName();
}
