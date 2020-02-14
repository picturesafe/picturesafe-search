/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTest {

    @Test
    public void testIntersect() {
        Object[] a = new Object[]{"a", "b", "c"};
        Object[] b = new Object[]{"b", "d", "e"};
        assertArrayEquals(new Object[]{"b"}, ArrayUtils.intersect(a, b));

        b = new Object[]{"d", "e"};
        assertArrayEquals(new Object[0], ArrayUtils.intersect(a, b));

        a = null;
        assertArrayEquals(new Object[0], ArrayUtils.intersect(a, b));

        a = new Object[]{"a", "b", "c"};
        b = null;
        assertArrayEquals(new Object[0], ArrayUtils.intersect(a, b));
    }

    @Test
    public void testUnion() {
        Object[] a = new Object[]{"a", "b", "c"};
        Object[] b = new Object[]{"b", "d", "e"};
        assertArrayEquals(new Object[]{"a", "b", "c", "d", "e"}, ArrayUtils.union(a, b));

        a = null;
        assertArrayEquals(b, ArrayUtils.union(a, b));

        a = new Object[]{"a", "b", "c"};
        b = null;
        assertArrayEquals(a, ArrayUtils.union(a, b));
    }

    @Test
    public void testComplement() {
        Object[] a = new Object[]{"a", "b", "c"};
        Object[] b = new Object[]{"b", "d", "e"};
        assertArrayEquals(new Object[]{"a", "c", "d", "e"}, ArrayUtils.complement(a, b));

        b = new Object[]{"a", "b", "c"};
        assertArrayEquals(new Object[0], ArrayUtils.complement(a, b));

        a = null;
        b = new Object[]{"b", "d", "e"};
        assertArrayEquals(b, ArrayUtils.complement(a, b));

        a = new Object[]{"a", "b", "c"};
        b = null;
        assertArrayEquals(a, ArrayUtils.complement(a, b));
    }
}
