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

package de.picturesafe.search.elasticsearch.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IdFormatTest {

    @Test
    public void testDefaultFormat() {
        assertEquals("1-2-3", IdFormat.DEFAULT.format("1-2-3"));
        assertEquals("123", IdFormat.DEFAULT.format(123));
        assertEquals("1234", IdFormat.DEFAULT.format(1234L));
        assertEquals("12.34", IdFormat.DEFAULT.format(12.34));
        assertEquals("12.345", IdFormat.DEFAULT.format(12.345D));

        assertEquals(123, IdFormat.DEFAULT.parse("123", Integer.class).intValue());
        assertEquals(1234, IdFormat.DEFAULT.parse("1234", Long.class).longValue());
        assertEquals(12.34, IdFormat.DEFAULT.parse("12.34", Float.class), 0.001);
        assertEquals(12.345, IdFormat.DEFAULT.parse("12.345", Double.class), 0.0001);
    }
}
