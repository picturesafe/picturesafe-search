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
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MustNotExpressionTest {

    @Test
    public void testOptimize() {
        MustNotExpression mustNotExpression = new MustNotExpression(new ValueExpression("test", 1));
        assertEquals(mustNotExpression, mustNotExpression.optimize());

        mustNotExpression = new MustNotExpression(new AbstractExpression() {
            @Override
            public Expression optimize() {
                return new EmptyExpression();
            }
        });
        assertEquals(new EmptyExpression(), mustNotExpression.optimize());

        mustNotExpression = new MustNotExpression(new AbstractExpression() {
            @Override
            public Expression optimize() {
                return null;
            }
        });
        assertEquals(new EmptyExpression(), mustNotExpression.optimize());
    }
}
