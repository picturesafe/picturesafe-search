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

import org.apache.commons.lang3.Validate;

/**
 * Format for formatting and parsing IDs
 */
public interface IdFormat {

    String format(Object id);

    <T> T parse(String id, Class<T> type);

    /**
     * Default format for formatting and parsing IDs
     */
    IdFormat DEFAULT = new IdFormat() {

        @Override
        public String format(Object id) {
            return (id != null) ? id.toString() : null;
        }

        @Override
        public <T> T parse(String id, Class<T> type) {
            if (id == null) {
                return null;
            }

            Validate.notNull(id, "Parameter 'type' may not be null!");
            final Object value;
            switch (type.getName()) {
                case "java.lang.String":
                    value = id;
                    break;
                case "java.lang.Long":
                    value = Long.valueOf(id);
                    break;
                case "java.lang.Integer":
                    value = Integer.valueOf(id);
                    break;
                case "java.lang.Double":
                    value = Double.valueOf(id);
                    break;
                case "java.lang.Float":
                    value = Float.valueOf(id);
                    break;
                default:
                    throw new RuntimeException("Unsupported id type: " + type);
            }

            return type.cast(value);
        }
    };
}
