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

package de.picturesafe.search.elasticsearch.connect.filter;

import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;

public interface FilterFactory {

    /**
     * Creates a List of {@link QueryBuilder} (filter).
     *
     * @param context {@link SearchContext}
     * @return        A List of {@link QueryBuilder} (filter) - may be empty
     */
    List<QueryBuilder> create(SearchContext context);
}
