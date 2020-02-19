package de.picturesafe.search.expression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InExpressionTest {

    @Test
    public void testOptimizeBatches() {
        final int size = 111;
        final Object[] values = new Object[111];
        for (int i = 0; i < size; i++) {
            values[i] = i + 1;
        }

        final InExpression inExpression = new InExpression("test", values);
        final OperationExpression operationExpression = inExpression.optimizeBatches(50);
        assertEquals(OperationExpression.Operator.OR, operationExpression.getOperator());
        assertEquals(3, operationExpression.getOperands().size());

        int value = 0;
        for (final Expression expression : operationExpression.getOperands()) {
            assertTrue(expression instanceof InExpression);
            for (final Object obj : ((InExpression) expression).getValues()) {
                assertEquals(++value, obj);
            }
        }
        assertEquals(size, value);
    }
}
