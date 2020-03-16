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

import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EnumSet;
import java.util.Set;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_ENDS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_STARTS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_WILDCARD;

/**
 * Expression to match values on keyword fields
 *
 * @see ConditionExpression
 */
public class KeywordExpression extends ValueExpression {

    private static final Set<Comparison> ALLOWED_COMPARISONS = EnumSet.of(EQ, NOT_EQ, TERM_STARTS_WITH, TERM_ENDS_WITH, TERM_WILDCARD);

    /**
     * Constructor
     * @param comparison Comparison operation
     */
    public KeywordExpression(Comparison comparison) {
        super(comparison);
    }

    /**
     * Constructor
     * @param name Field name
     * @param value Value to match
     */
    public KeywordExpression(String name, Object value) {
        this(name, EQ, value);
    }

    /**
     * Constructor
     * @param name Field name
     * @param comparison Comparison operation
     * @param value Value to match
     */
    public KeywordExpression(String name, Comparison comparison, Object value) {
        super(name, comparison, value);
        validateComparison();
    }

    private void validateComparison() {
        Validate.isTrue(ALLOWED_COMPARISONS.contains(comparison), "Unsupported comparison for keyword expressions: " + comparison);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeywordExpression)) {
            return false;
        }

        final KeywordExpression that = (KeywordExpression) o;
        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .appendSuper(super.toString())
                .toString();
    }
}
