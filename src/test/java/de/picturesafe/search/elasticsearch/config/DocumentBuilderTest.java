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

package de.picturesafe.search.elasticsearch.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DocumentBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testDocumentBuilder() {
        Map<String, Object> document = DocumentBuilder.id(1).put("title", "This is a title").toDcoument();
        assertTrue(document.get(FieldConfiguration.FIELD_NAME_ID) instanceof Long);
        assertEquals(1, (long) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));

        document = DocumentBuilder.id(1000).put("title", "This is a title").put("caption", "This is a caption").toDcoument();
        assertEquals(1000, (long) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));
        assertEquals("This is a caption", document.get("caption"));

        document = DocumentBuilder.id(1).put("title", "This is a title").put("available", true).toDcoument();
        assertEquals(1, (long) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));
        assertTrue(document.get("available") instanceof Boolean);
        assertTrue((Boolean) document.get("available"));

        document = DocumentBuilder.id(1).put("title", null).toDcoument();
        assertEquals(1, (long) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertNull(document.get("title"));

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Argument 'id' must be > 0!");
        DocumentBuilder.id(-1).put("title", null).toDcoument();
    }
}
