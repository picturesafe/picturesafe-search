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
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.parameter.SortOption;
import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.picturesafe.search.elasticsearch.connect.filter.util.FilterFactoryUtils.createFilter;
import static de.picturesafe.search.elasticsearch.connect.util.QueryBuilderUtils.applyBoost;
import static de.picturesafe.search.parameter.SortOption.RELEVANCE_NAME;

@Component
public class RelevanceSortQueryFactory implements QueryFactory {

    private final List<FilterFactory> filterFactories;

    @Autowired
    public RelevanceSortQueryFactory(List<FilterFactory> filterFactories) {
        this.filterFactories = filterFactories;
    }

    @Override
    public boolean supports(SearchContext context) {
        if (isRelevanceSort(context)) {
            Expression expression = context.getRootExpression();
            if (expression instanceof MustNotExpression) {
                expression = ((MustNotExpression) expression).getExpression();
            }
            return !context.isProcessed(expression);
        }
        return false;
    }

    private boolean isRelevanceSort(SearchContext context) {
        final List<SortOption> sortOptions = context.getSortOptions();
        return CollectionUtils.isNotEmpty(sortOptions) && sortOptions.stream().anyMatch(s -> s.getFieldName().equals(RELEVANCE_NAME));
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, SearchContext context) {
        Expression expression = context.getRootExpression();
        if (expression instanceof MustNotExpression) {
            expression = ((MustNotExpression) expression).getExpression();
        }

        QueryBuilder queryBuilder = createFilter(filterFactories, context);
        applyBoost(queryBuilder, expression);
        context.setProcessed(expression);
        return queryBuilder;
    }
}
