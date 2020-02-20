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

package de.picturesafe.search.elasticsearch.connect.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class StringTrimUtility {

    /**
     * Trims all string values of the given list,
     * if the first element of the list contains a string value.
     * If the first element of the list is not a string or the list is empty,
     * the original list is returned.
     *
     * @param list  List to be checked
     * @return      list with trimmed strings
     */
    public static List<?> trimListValues(List<?> list) {
        if (list == null
                || list.isEmpty()
                || !(list.get(0) instanceof String)) {
            return list;
        }

        final List<String> newList = new ArrayList<>();

        for (Object o : list) {
            if (o instanceof String) {
                final String s = (String) o;
                if (StringUtils.isNotEmpty(s)) {
                    newList.add(s.trim());
                } else {
                    newList.add(s);
                }
            } else {
                throw new RuntimeException("Given list must contain string values in all entries!");
            }
        }
        return newList;
    }
}
