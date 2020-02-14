/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.util;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StringTrimUtilityTest {

    /**
     * Testet das Trimmen von nicht typisierten Listen
     */
    @Test
    public void testTrimStringList() {
        final List<String> l = new ArrayList<>();
        l.add("Entry");
        l.add(" Entry");
        l.add(" Entry ");
        l.add("  Entry");
        l.add("  Entry  ");
        final List<?> newList = StringTrimUtility.trimListValues(l);

        assertTrue("List is not empty", newList != null && newList.size() > 0);
        assertEquals("List contains 5 elements", 5, newList.size());
        assertTrue("First element of List is a String", newList.get(0) instanceof String);

        for (Object o : newList) {
            assertTrue("Element of List is a String", o instanceof String);

            final String s = (String) o;
            assertEquals("Entry quals 'Entry'", "Entry", s);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidList() {
        final List<Object> l = new ArrayList<>();
        l.add("Entry");
        l.add(1);
        StringTrimUtility.trimListValues(l);
    }

    @Test
    public void testListWithoutStrings() {
        final List<Object> l = new ArrayList<>();
        l.add(true);
        l.add(1);
        l.add(new BigDecimal(1));
        final List<?> newList = StringTrimUtility.trimListValues(l);

        assertTrue("List is not empty", newList != null && newList.size() > 0);
        assertEquals("List contains 3 elements", 3, newList.size());
        assertTrue("First element is true", (Boolean) newList.get(0));
        assertEquals("Second element has value 1", 1, newList.get(1));
        assertTrue("Third element is instance of BigDecimal and has value 1",
                newList.get(2) instanceof BigDecimal && ((BigDecimal) newList.get(2)).intValue() == 1);
    }

    @Test
    public void testEmptyList() {
        final List<?> l = new ArrayList<>();
        final List<?> newList = StringTrimUtility.trimListValues(l);

        assertTrue("List is empty", newList != null && newList.size() == 0);
    }
}
