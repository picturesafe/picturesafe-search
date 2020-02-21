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
