/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
