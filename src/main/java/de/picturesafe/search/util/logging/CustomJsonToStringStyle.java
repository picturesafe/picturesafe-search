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

package de.picturesafe.search.util.logging;

import org.apache.commons.lang3.builder.ToStringStyle;

public class CustomJsonToStringStyle extends ToStringStyle {

    public CustomJsonToStringStyle() {
        setUseShortClassName(true);
        setUseIdentityHashCode(false);

        setContentStart(":{");
        setContentEnd("}");

        setArrayStart("[");
        setArrayEnd("]");

        setFieldSeparator(",");
        setFieldNameValueSeparator(":");

        setNullText("null");

        setSummaryObjectStartText("\"<");
        setSummaryObjectEndText(">\"");

        setSizeStartText("\"<size=");
        setSizeEndText(">\"");
    }

    @Override
    protected void appendFieldStart(StringBuffer buffer, String fieldName) {
        if (isUseFieldNames() && fieldName != null) {
            buffer.append("\"");
            buffer.append(fieldName);
            buffer.append("\"");
            buffer.append(getFieldNameValueSeparator());
        }
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (value instanceof Number || value instanceof Boolean) {
            super.appendDetail(buffer, fieldName, value);
        } else {
            buffer.append("\"").append(value).append("\"");
        }
    }
}
