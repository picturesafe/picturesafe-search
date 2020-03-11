package de.picturesafe.search.expression;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class RangeValueExpressionTest {

    @Test
    public void testRangeValueExpression() {
        RangeValueExpression rangeValueExpression = new RangeValueExpression();
        rangeValueExpression.setName("test");
        rangeValueExpression.setMinValue(1);
        rangeValueExpression.setMaxValue(100);
        assertEquals(RangeValueExpression.Comparison.BETWEEN, rangeValueExpression.getComparison());
        assertEquals("test", rangeValueExpression.getName());
        assertEquals(1, rangeValueExpression.getMinValue());
        assertEquals(100, rangeValueExpression.getMaxValue());

        rangeValueExpression = new RangeValueExpression("test", 1, 100);
        assertEquals(RangeValueExpression.Comparison.BETWEEN, rangeValueExpression.getComparison());
        assertEquals("test", rangeValueExpression.getName());
        assertEquals(1, rangeValueExpression.getMinValue());
        assertEquals(100, rangeValueExpression.getMaxValue());
    }

    @Test
    public void testDayRangeValueExpression() throws Exception {
        final Date day1 = new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2020");
        final Date day2 = new SimpleDateFormat("dd.MM.yyyy").parse("31.12.2020");

        DayRangeExpression dayRangeExpression = new DayRangeExpression();
        dayRangeExpression.setName("test");
        dayRangeExpression.setFromDay(day1);
        dayRangeExpression.setUntilDay(day2);
        assertEquals(RangeValueExpression.Comparison.BETWEEN, dayRangeExpression.getComparison());
        assertEquals("test", dayRangeExpression.getName());
        assertEquals(day1, dayRangeExpression.getFromDay());
        assertEquals(day2, dayRangeExpression.getUntilDay());

        dayRangeExpression = new DayRangeExpression("test", day1, day2);
        assertEquals(RangeValueExpression.Comparison.BETWEEN, dayRangeExpression.getComparison());
        assertEquals("test", dayRangeExpression.getName());
        assertEquals(day1, dayRangeExpression.getFromDay());
        assertEquals(day2, dayRangeExpression.getUntilDay());
    }
}
