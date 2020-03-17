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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.NOT_EQ;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_ENDS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_STARTS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_WILDCARD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class KeywordExpressionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordExpressionTest.class);
    private static final EnumSet<ConditionExpression.Comparison> VALID_COMPARISONS = EnumSet.of(EQ, NOT_EQ, TERM_STARTS_WITH, TERM_ENDS_WITH, TERM_WILDCARD);

    @Test
    public void testAllowedComparisons() {
        VALID_COMPARISONS.forEach(comparison -> {
            LOGGER.debug("Testing comparison: {}", comparison);
            final KeywordExpression expression = new KeywordExpression("test", comparison, "anything");
            assertEquals(comparison, expression.getComparison());
        });
    }

    @Test
    public void testNotAllowedComparisons() {
        EnumSet.complementOf(VALID_COMPARISONS).forEach(comparison -> {
            LOGGER.debug("Testing comparison: {}", comparison);
            try {
                new KeywordExpression("test", comparison, "anything");
                fail("IllegalArgumentException should be thrown");
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Caught expected exception", e);
            }
        });
    }
}
