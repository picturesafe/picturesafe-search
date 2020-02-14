/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.expression;

import de.picturesafe.search.expression.internal.EmptyExpression;
import de.picturesafe.search.expression.internal.FalseExpression;
import de.picturesafe.search.expression.internal.TrueExpression;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class OperationExpressionTest {

    private static final String STANDARD_DAY_FORMAT = "dd.MM.yyyy";

    @Test
    public void testEmptyExpressionOptimization() {
        OperationExpression op = OperationExpression.builder().add(new EmptyExpression()).add(new EmptyExpression()).build();
        Expression optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof EmptyExpression);

        op = OperationExpression.builder()
                .add(new EmptyExpression())
                .add(new EmptyExpression())
                .add(new ValueExpression("test1", "test1")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof ValueExpression);

        final List<Expression> expressions = new ArrayList<>();
        expressions.add(new EmptyExpression());
        expressions.add(new EmptyExpression());
        expressions.add(new ValueExpression("test1", "test1"));
        expressions.add(new ValueExpression("test2", "test2"));
        op = OperationExpression.builder(OperationExpression.Operator.AND, expressions).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof OperationExpression);
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);

        op = OperationExpression.builder()
                .add(new ValueExpression("test3", "test3"))
                .addAll(expressions).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof OperationExpression);
        assertEquals(3, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);

        op = OperationExpression.builder(OperationExpression.Operator.AND, expressions).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof OperationExpression);
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);
    }

    @Test
    public void testTrueExpressionOptimization() {
        OperationExpression op = OperationExpression.builder(OperationExpression.Operator.OR).add(new TrueExpression()).build();
        Expression optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof TrueExpression);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new TrueExpression())
                .add(new ValueExpression("test1", "test1")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof TrueExpression);

        op = OperationExpression.builder().add(new TrueExpression()).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof TrueExpression);

        op = OperationExpression.builder()
                .add(new TrueExpression())
                .add(new ValueExpression("test1", "test1")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof ValueExpression);

        op = OperationExpression.builder()
                .add(new TrueExpression())
                .add(new ValueExpression("test1", "test1"))
                .add(new ValueExpression("test2", "test2")).build();
        optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);
    }

    @Test
    public void testFalseExpressionOptimization() {
        OperationExpression op = OperationExpression.builder()
                .add(new FalseExpression()).build();
        Expression optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof FalseExpression);

        op = OperationExpression.builder()
                .add(new FalseExpression())
                .add(new ValueExpression("test1", "test1")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof FalseExpression);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new FalseExpression()).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof FalseExpression);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new FalseExpression())
                .add(new ValueExpression("test1", "test1")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof ValueExpression);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new FalseExpression())
                .add(new ValueExpression("test1", "test1"))
                .add(new ValueExpression("test2", "test2")).build();
        optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);
    }

    @Test
    public void testInExpressionOptimization() {

        final int[] intArray1 = new int[]{1, 2, 3};
        final int[] intArray2 = new int[]{3, 4, 5};

        OperationExpression op = OperationExpression.builder()
                .add(new InExpression("intArray1", intArray1))
                .add(new InExpression("intArray2", intArray2)).build();
        Expression optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof InExpression);
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(1) instanceof InExpression);

        op = OperationExpression.builder()
                .add(new InExpression("intArray", intArray1))
                .add(new InExpression("intArray", intArray2)).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof InExpression);
        assertEquals(1, ((InExpression) optimzedExpression).getValues().length);
        assertEquals(3, ((InExpression) optimzedExpression).getValues()[0]);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new InExpression("intArray", intArray1))
                .add(new InExpression("intArray", intArray2)).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof InExpression);
        assertEquals(5, ((InExpression) optimzedExpression).getValues().length);

        op = OperationExpression.builder()
                .add(new InExpression("stringArray1", "A", "B", "C"))
                .add(new InExpression("stringArray2", "C", "D", "E")).build();
        optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof InExpression);
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(1) instanceof InExpression);

        op = OperationExpression.builder()
                .add(new InExpression("stringArray", "A", "B", "C"))
                .add(new InExpression("stringArray", "C", "D", "E")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof InExpression);
        assertEquals(1, ((InExpression) optimzedExpression).getValues().length);
        assertEquals("C", ((InExpression) optimzedExpression).getValues()[0]);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new InExpression("stringArray", "A", "B", "C"))
                .add(new InExpression("stringArray", "C", "D", "E")).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof InExpression);
        assertEquals(5, ((InExpression) optimzedExpression).getValues().length);
    }

    @Test
    public void testMustNotExpressionOptimization() {

        final int[] intArray1 = new int[]{1, 2, 3};
        final int[] intArray2 = new int[]{3, 4, 5};

        OperationExpression op = OperationExpression.builder()
                .add(new MustNotExpression(new InExpression("intArray1", intArray1)))
                .add(new MustNotExpression(new InExpression("intArray2", intArray2))).build();
        Expression optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof MustNotExpression);
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(1) instanceof MustNotExpression);

        op = OperationExpression.builder()
                .add(new MustNotExpression(new InExpression("intArray", intArray1)))
                .add(new MustNotExpression(new InExpression("intArray", intArray2))).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof MustNotExpression);
        assertTrue(((MustNotExpression) optimzedExpression).getExpression() instanceof InExpression);
        assertEquals(5, ((InExpression) (((MustNotExpression) optimzedExpression)).getExpression()).getValues().length);

        op = OperationExpression.builder(OperationExpression.Operator.OR)
                .add(new MustNotExpression(new InExpression("intArray", intArray1)))
                .add(new MustNotExpression(new InExpression("intArray", intArray2))).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof MustNotExpression);
        assertEquals(1, ((InExpression) (((MustNotExpression) optimzedExpression)).getExpression()).getValues().length);
        assertEquals(3, ((InExpression) (((MustNotExpression) optimzedExpression)).getExpression()).getValues()[0]);
    }

    @Test
    public void testRangeValueExpressionOptimization() throws Exception {

        final Date day1 = new SimpleDateFormat(STANDARD_DAY_FORMAT).parse("26.04.2017");
        final Date day2 = new SimpleDateFormat(STANDARD_DAY_FORMAT).parse("30.04.2017");

        OperationExpression op = OperationExpression.builder()
                .add(new ValueExpression("dayfield1", ConditionExpression.Comparison.GE, day1))
                .add(new ValueExpression("dayfield2", ConditionExpression.Comparison.LE, day2)).build();
        Expression optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof ValueExpression);
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(1) instanceof ValueExpression);

        op = OperationExpression.builder()
                .add(new ValueExpression("dayfield", ConditionExpression.Comparison.GE, day1))
                .add(new ValueExpression("dayfield", ConditionExpression.Comparison.LE, day2)).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof RangeValueExpression);
    }

    @Test
    public void testDayRangeValueExpressionOptimization() throws Exception {
        final Date day1 = new SimpleDateFormat(STANDARD_DAY_FORMAT).parse("26.04.2017");
        final Date day2 = new SimpleDateFormat(STANDARD_DAY_FORMAT).parse("30.04.2017");

        OperationExpression op = OperationExpression.builder()
                .add(new DayExpression("dayfield1", ConditionExpression.Comparison.GE, day1))
                .add(new DayExpression("dayfield2", ConditionExpression.Comparison.LE, day2)).build();
        Expression optimzedExpression = op.optimize();
        assertEquals(2, ((OperationExpression) optimzedExpression).getOperands().size());
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(0) instanceof DayExpression);
        assertTrue(((OperationExpression) optimzedExpression).getOperands().get(1) instanceof DayExpression);

        op = OperationExpression.builder()
                .add(new DayExpression("dayfield", ConditionExpression.Comparison.GE, day1))
                .add(new DayExpression("dayfield", ConditionExpression.Comparison.LE, day2)).build();
        optimzedExpression = op.optimize();
        assertTrue(optimzedExpression instanceof DayRangeExpression);
    }
}
