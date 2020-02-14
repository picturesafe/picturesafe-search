/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.util;

import java.util.HashSet;
import java.util.Set;

public class SetUtils extends org.apache.commons.collections.SetUtils {

    private SetUtils() {
    }

    public static <T> Set<T> complement(Set<T> setA, Set<T> setB) {
		return minus(union(setA, setB), intersect(setA, setB));
	}

	public static <T> Set<T> intersect(Set<T> setA, Set<T> setB) {
		final Set<T> set = new HashSet<>(setA);
		set.retainAll(setB);
		return set;
	}

	public static <T> Set<T> minus(Set<T> setA, Set<T> setB) {
		final Set<T> set = new HashSet<>(setA);
		set.removeAll(setB);
		return set;
	}

	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		final Set<T> set = new HashSet<>(setA);
		set.addAll(setB);
		return set;
	}
}
