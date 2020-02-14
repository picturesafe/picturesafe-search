/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SetUtilsTest {

    @Test
    public void testIntersect() {
        Set<String> a = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> b = new HashSet<>(Arrays.asList("b", "d", "e"));
        assertEquals(new HashSet<>(Collections.singletonList("b")), SetUtils.intersect(a, b));

        b = new HashSet<>(Arrays.asList("d", "e"));
        assertEquals(new HashSet<>(), SetUtils.intersect(a, b));

        a = new HashSet<>();
        assertEquals(new HashSet<>(), SetUtils.intersect(a, b));

        a = new HashSet<>(Arrays.asList("a", "b", "c"));
        b = new HashSet<>();
        assertEquals(new HashSet<>(), SetUtils.intersect(a, b));
    }

    @Test
    public void testUnion() {
        Set<String> a = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> b = new HashSet<>(Arrays.asList("b", "d", "e"));
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d", "e")), SetUtils.union(a, b));

        a = new HashSet<>();
        assertEquals(b, SetUtils.union(a, b));

        a = new HashSet<>(Arrays.asList("a", "b", "c"));
        b = new HashSet<>();
        assertEquals(a, SetUtils.union(a, b));
    }

    @Test
    public void testComplement() {
        Set<String> a = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> b = new HashSet<>(Arrays.asList("b", "d", "e"));
        assertEquals(new HashSet<>(Arrays.asList("a", "c", "d", "e")), SetUtils.complement(a, b));

        b = new HashSet<>(Arrays.asList("a", "b", "c"));
        assertEquals(new HashSet<>(), SetUtils.complement(a, b));

        a = new HashSet<>();
        b = new HashSet<>(Arrays.asList("b", "d", "e"));
        assertEquals(b, SetUtils.complement(a, b));

        a = new HashSet<>(Arrays.asList("a", "b", "c"));
        b = new HashSet<>();
        assertEquals(a, SetUtils.complement(a, b));
    }
}
