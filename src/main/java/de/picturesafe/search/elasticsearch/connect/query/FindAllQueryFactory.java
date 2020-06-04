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

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.connect.context.SearchContext;
import de.picturesafe.search.expression.FindAllExpression;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Component;

@Component
public class FindAllQueryFactory implements QueryFactory {

    @Override
    public boolean supports(SearchContext context) {
        return context.getRootExpression() instanceof FindAllExpression;
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, SearchContext context) {
        context.setRootExpressionProcessed();
        return QueryBuilders.matchAllQuery();
    }
}
