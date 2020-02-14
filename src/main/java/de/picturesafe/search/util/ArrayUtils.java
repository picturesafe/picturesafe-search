/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Creates the intersection between two arrays.
     * The intersection contains all values that are contained in both sets - each value only once.
     * The original order of the values is not retained, and the result set is sorted in ascending order.
     *
     * @param a1    First array from which the values are taken, null is treated as an empty array
     * @param a2    Second array in which the values must also be contained, null is handled as an empty array
     * @return      All values from the first array that are also contained in the second
     */
    public static Object[] intersect(Object[] a1, Object[] a2) {
        final Set<Object> s1 = toSet(a1);
        final List<Object> l2 = isNotEmpty(a2) ? Arrays.asList(a2) : Collections.emptyList();
        s1.retainAll(l2);
        return s1.toArray();
    }

    /**
     * Builds the union between two arrays.
     * The result contains all values that are contained in at least one of the two arrays - each value only once.
     * The original order of the values is not retained and the result array is sorted in ascending order.
     *
     * @param a1    First array from which the values are taken, null is treated as an empty array
     * @param a2    Second array in which the values must also be contained, null is handled as an empty array
     * @return      Union of all values from the first and second array.
     */
    public static Object[] union(Object[] a1, Object[] a2) {
        final Set<Object> result = toSet(a1);
        if (isNotEmpty(a2)) {
            result.addAll(Arrays.asList(a2));
        }
        return result.toArray();
    }

    /**
     * Mutual exclusion of the contents of two arrays.
     * The result array contains those values that occur ONLY in one or the other array.
     *
     * @param a1    First array from which the values are taken, null is treated as an empty array
     * @param a2    Second array in which the values must also be contained, null is handled as an empty array
     * @return      Array with the entries from both given arrays, which are only contained in one of the arrays
     */
    public static Object[] complement(Object[] a1, Object[] a2) {
        final Set<Object> set1 = toSet(a1);
        final Set<Object> set2 = toSet(a2);
        return SetUtils.complement(set1, set2).toArray();
    }

    private static Set<Object> toSet(Object[] a) {
        final Set<Object> s = new HashSet<>();
        if (isNotEmpty(a)) {
            s.addAll(Arrays.asList(a));
        }
        return s;
    }
}
