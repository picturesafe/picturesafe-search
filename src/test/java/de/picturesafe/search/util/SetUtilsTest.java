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
