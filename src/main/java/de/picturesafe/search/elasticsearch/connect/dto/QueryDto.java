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

package de.picturesafe.search.elasticsearch.connect.dto;

import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.parameter.AccountContext;
import de.picturesafe.search.parameter.SearchAggregation;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Transports the data for a search query.
 */
public class QueryDto {

    public enum FieldResolverType {
        DOC_VALUES,
        SOURCE_VALUES
    }

    private final Expression expression;
    private final Locale locale;

    private QueryRangeDto queryRange;
    private List<SortOption> sortOptions = Collections.emptyList();
    private List<? extends SearchAggregation> aggregations = Collections.emptyList();
    private List<String> fieldsToResolve = Collections.emptyList();
    private FieldResolverType fieldResolverType = FieldResolverType.DOC_VALUES;
    private AccountContext<?> accountContext;
    private boolean sortFilter;

    public QueryDto(Expression expression, Locale locale) {
        this.expression = expression;
        this.locale = locale;
    }

    public QueryDto(Expression expression,
                    QueryRangeDto queryRange,
                    List<SortOption> sortOptions,
                    List<? extends SearchAggregation> aggregations,
                    Locale locale) {
        this.expression = expression;
        this.queryRange = queryRange;
        this.sortOptions = sortOptions;
        this.aggregations = aggregations;
        this.locale = locale;
    }

    public QueryDto(Expression expression,
                    QueryRangeDto queryRange,
                    List<SortOption> sortOptions,
                    List<? extends SearchAggregation> aggregations,
                    Locale locale,
                    List<String> fieldsToResolve,
                    FieldResolverType fieldResolverType) {
        this.expression = expression;
        this.queryRange = queryRange;
        this.sortOptions = sortOptions;
        this.aggregations = aggregations;
        this.locale = locale;
        this.fieldsToResolve = fieldsToResolve;
        this.fieldResolverType = fieldResolverType;
    }

    public QueryDto(QueryDto queryDto, Expression expression) {
        this(expression, queryDto.queryRange, queryDto.sortOptions, queryDto.aggregations, queryDto.locale, queryDto.fieldsToResolve,
                queryDto.fieldResolverType);
    }

    public Expression getExpression() {
        return expression;
    }

    public Locale getLocale() {
        return locale;
    }

    public QueryRangeDto getQueryRange() {
        return queryRange;
    }

    public QueryDto queryRange(QueryRangeDto queryRangeDto) {
        this.queryRange = queryRangeDto;
        return this;
    }

    public List<SortOption> getSortOptions() {
        return sortOptions;
    }

    public QueryDto sortOptions(List<SortOption> sortOptions) {
        this.sortOptions = sortOptions;
        return this;
    }

    public List<? extends SearchAggregation> getAggregations() {
        return aggregations;
    }

    public QueryDto aggregations(List<? extends SearchAggregation> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    public List<String> getFieldsToResolve() {
        return fieldsToResolve;
    }

    public QueryDto fieldsToResolve(List<String> fieldsToResolve) {
        this.fieldsToResolve = fieldsToResolve;
        return this;
    }

    public FieldResolverType getFieldResolverType() {
        return fieldResolverType;
    }

    public QueryDto fieldResolverType(FieldResolverType fieldResolverType) {
        this.fieldResolverType = fieldResolverType;
        return this;
    }

    public AccountContext<?> getAccountContext() {
        return accountContext;
    }

    public QueryDto withAccountContext(AccountContext<?> accountContext) {
        this.accountContext = accountContext;
        return this;
    }

    public boolean isSortFilter() {
        return sortFilter;
    }

    public QueryDto sortFilter(boolean sortFilter) {
        this.sortFilter = sortFilter;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(expression).append(locale).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryDto)) {
            return false;
        } else {
            final QueryDto other = (QueryDto) o;
            return new EqualsBuilder()
                    .append(expression, other.expression)
                    .append(locale, other.locale)
                    .append(queryRange, other.queryRange)
                    .append(sortOptions, other.sortOptions)
                    .append(aggregations, other.aggregations)
                    .append(fieldsToResolve, other.fieldsToResolve)
                    .isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("expression", expression) //--
                .append("locale", locale) //--
                .append("queryRange", queryRange) //--
                .append("sortOptions", sortOptions) //--
                .append("aggregations", aggregations) //--
                .append("fieldsToResolve", fieldsToResolve) //--
                .append("fieldResolverType", fieldResolverType) //--
                .toString();
    }

    public static QueryDto sortFilter(Expression expression, Locale locale) {
        return new QueryDto(expression, locale).sortFilter(true);
    }
}
