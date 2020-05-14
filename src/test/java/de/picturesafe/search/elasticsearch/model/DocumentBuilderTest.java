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

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;
import java.util.Map;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getInt;
import static de.picturesafe.search.elasticsearch.connect.util.ElasticDocumentUtils.getString;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DocumentBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void test() {
        Map<String, Object> document = DocumentBuilder.id(1).put("title", "This is a title").build();
        assertTrue(document.get(FieldConfiguration.FIELD_NAME_ID) instanceof String);
        assertEquals("1", (String) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));

        document = DocumentBuilder.id(1000).put("title", "This is a title").put("caption", "This is a caption").build();
        assertEquals("1000", (String) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));
        assertEquals("This is a caption", document.get("caption"));

        document = DocumentBuilder.id(1).put("title", "This is a title").put("available", true).build();
        assertEquals("1", (String) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertEquals("This is a title", document.get("title"));
        assertTrue(document.get("available") instanceof Boolean);
        assertTrue((Boolean) document.get("available"));

        document = DocumentBuilder.id(1).put("title", (String) null).build();
        assertEquals("1", (String) document.get(FieldConfiguration.FIELD_NAME_ID));
        assertNull(document.get("title"));

        final Date date = new Date();
        document = DocumentBuilder.withoutId().put("date", date).build();
        assertEquals(date, ElasticDateUtils.parseIso((String) document.get("date")));

        exception.expect(NullPointerException.class);
        exception.expectMessage("Parameter 'id' may not be null!");
        DocumentBuilder.id(null).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPutIndexObject() {
        final TestObject testObject = new TestObject("the text", 17);
        final Map<String, Object> document = DocumentBuilder.id(1).put("title", "This is a title").put("testObject", testObject).build();
        assertEquals("This is a title", document.get("title"));
        final Object testDoc = document.get("testObject");
        assertTrue(testDoc instanceof Map);
        final TestObject docObject = new TestObject().fromDocument((Map<String, Object>) testDoc);
        assertEquals(testObject, docObject);
    }

    private static class TestObject implements IndexObject<TestObject> {

        private String textField;
        private int numberField;

        public TestObject() {
        }

        public TestObject(String textField, int numberField) {
            this.textField = textField;
            this.numberField = numberField;
        }

        @Override
        public Map<String, Object> toDocument() {
            return DocumentBuilder.withoutId().put("textField", textField).put("numberField", numberField).build();
        }

        @Override
        public TestObject fromDocument(Map<String, Object> document) {
            textField = getString(document, "textField");
            numberField = getInt(document, "numberField", 0);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TestObject that = (TestObject) o;
            return new EqualsBuilder().append(numberField, that.numberField).append(textField, that.textField).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(textField).toHashCode();
        }
    }
}
