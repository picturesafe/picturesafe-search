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

package de.picturesafe.search.expression;

import de.picturesafe.search.expression.internal.FalseExpression;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Expression to match a set of values
 */
public class InExpression extends AbstractExpression implements FieldExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(InExpression.class);

    private static final int MAX_SIZE = 65_536; // Limit by elasticsearch terms query

    private String name = "";
    private Object[] values;

    /**
     * Default constructor
     */
    public InExpression() {
    }

    /**
     * Constructor
     * @param name Field name
     * @param values Values to match
     */
    public InExpression(String name, Object... values) {
        this.name = name;
        this.values = values;
    }

    /**
     * Constructor
     * @param name Field name
     * @param ids IDs to match
     */
    public InExpression(String name, int[] ids) {
        this.name = name;
        this.values = ArrayUtils.toObject(ids);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the field name
     * @param name Filed name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the values to match
     * @return Values to match
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Sets the values to match
     * @param values Values to match
     */
    public void setValues(Object[] values) {
        this.values = values;
    }

    /**
     * Sets the IDs to match
     * @param ids IDs to match
     */
    public void setIds(int[] ids) {
        this.values = ArrayUtils.toObject(ids);
    }

    @Override
    public Expression optimize() {
        if (ArrayUtils.isEmpty(values)) {
            return new FalseExpression();
        } else if (values.length > MAX_SIZE) {
            LOGGER.warn("Number of values [{}] exceeds the limit by elasticsearch [{}], will split expression in smaller batches.", values.length, MAX_SIZE);
            return optimizeBatches(MAX_SIZE / 2);
        } else {
            return this;
        }
    }

    OperationExpression optimizeBatches(int batchSize) {
        final OperationExpression.Builder opeBuilder = OperationExpression.builder(OperationExpression.Operator.OR);
        for (int i = 0; i < values.length; i += currentBatchSize(batchSize, i)) {
            final int size = currentBatchSize(batchSize, i);
            final Object[] batch = new Object[size];
            System.arraycopy(values, i, batch, 0, size);
            opeBuilder.add(new InExpression(name, batch));
        }
        return opeBuilder.build();
    }

    private int currentBatchSize(int batchSize, int valuesIdx) {
        return Math.min(batchSize, values.length - valuesIdx);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final InExpression that = (InExpression) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(values, that.values)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("name", name)
                .append("values", valuesToString(10))
                .toString();
    }

    private Object valuesToString(int maxElements) {
        if (ArrayUtils.isNotEmpty(values) && values.length > maxElements) {
            final Object[] subArray = ArrayUtils.subarray(values, 0, maxElements);
            final String suffix = (values.length > subArray.length) ? ",..." : "";
            return "[" + ArrayUtils.toString(subArray) + suffix + "]";
        } else {
            return values;
        }
    }
}
