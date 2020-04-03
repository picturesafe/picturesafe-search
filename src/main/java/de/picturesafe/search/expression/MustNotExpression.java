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

import de.picturesafe.search.expression.internal.EmptyExpression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Expression defining a negation of another expression
 */
public class MustNotExpression extends AbstractExpression {

    private Expression expression;

    /**
     * Constructor
     * @param expression Expression to negate
     */
    public MustNotExpression(Expression expression) {
        Validate.notNull(expression, "Parameter 'expression' may not be null!");
        this.expression = expression;
        expression.setParent(this);
    }

    /**
     * Gets the expression to negate.
     * @return Expression to negate
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public Expression optimize() {
        Expression optimizedExpression = expression.optimize();
        if (optimizedExpression == null) {
            optimizedExpression = new EmptyExpression();
        }
        return (!(optimizedExpression instanceof EmptyExpression)) ? new MustNotExpression(optimizedExpression) : optimizedExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MustNotExpression that = (MustNotExpression) o;
        return new EqualsBuilder().append(expression, that.expression).isEquals();
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("expression", expression) //--
                .toString();
    }
}
