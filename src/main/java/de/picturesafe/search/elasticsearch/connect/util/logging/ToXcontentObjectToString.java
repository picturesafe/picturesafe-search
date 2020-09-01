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

package de.picturesafe.search.elasticsearch.connect.util.logging;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.ToXContentObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import java.io.IOException;

public class ToXcontentObjectToString {

    private final ToXContentObject toXcontentObject;

    public ToXcontentObjectToString(ToXContentObject toXcontentObject) {
        this.toXcontentObject = toXcontentObject;
    }

    @Override
    public String toString() {
        try {
            final XContentBuilder xContent = toXcontentObject.toXContent(JsonXContent.contentBuilder(), ToXContent.EMPTY_PARAMS);
            return Strings.toString(xContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
