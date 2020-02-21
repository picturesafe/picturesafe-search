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
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
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
    private final List<QueryFilterDto> queryFilterDtos;
    private final QueryRangeDto queryRangeDto;
    private final List<SortOption> sortOptions;
    private final List<QueryFacetDto> queryFacetDtos;
    private final Locale locale;
    private final List<String> fieldsToResolve;
    private final FieldResolverType fieldResolverType;

    public QueryDto(Expression expression,
                    QueryRangeDto queryRangeDto,
                    List<QueryFilterDto> queryFilterDtos,
                    List<SortOption> sortOptions,
                    List<QueryFacetDto> queryFacetDtos,
                    Locale locale) {
        this(expression, queryRangeDto, queryFilterDtos, sortOptions, queryFacetDtos, locale,
                new ArrayList<>(), FieldResolverType.DOC_VALUES);
    }

    public QueryDto(Expression expression,
                    QueryRangeDto queryRangeDto,
                    List<QueryFilterDto> queryFilterDtos,
                    List<SortOption> sortOptions,
                    List<QueryFacetDto> queryFacetDtos,
                    Locale locale,
                    List<String> fieldsToResolve,
                    FieldResolverType fieldResolverType) {
        this.expression = expression;
        this.queryRangeDto = queryRangeDto;
        this.queryFilterDtos = queryFilterDtos;
        this.sortOptions = sortOptions;
        this.queryFacetDtos = queryFacetDtos;
        this.locale = locale;
        this.fieldsToResolve = fieldsToResolve;
        this.fieldResolverType = fieldResolverType;

        Validate.notNull(queryRangeDto, "Search Result Range (QueryRangeDTO) is not allowed to be null.");
    }

    public Expression getExpression() {
        return expression;
    }

    public QueryRangeDto getQueryRangeDto() {
        return queryRangeDto;
    }

    public List<QueryFilterDto> getQueryFilterDtos() {
        return queryFilterDtos;
    }

    public List<SortOption> getSortOptions() {
        return sortOptions;
    }

    public List<QueryFacetDto> getQueryFacetDtos() {
        return queryFacetDtos;
    }

    public Locale getLocale() {
        return locale;
    }

    public List<String> getFieldsToResolve() {
        return fieldsToResolve;
    }

    public FieldResolverType getFieldResolverType() {
        return fieldResolverType;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(659, 293).append(expression).append(queryRangeDto).append(queryFilterDtos)
                .append(sortOptions).append(queryFacetDtos).append(locale).append(fieldsToResolve).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof QueryDto)) {
            return false;
        } else {
            final QueryDto target = (QueryDto) o;
            return new EqualsBuilder().append(expression, target.getExpression()).append(queryRangeDto, target.getQueryRangeDto())
                    .append(queryFilterDtos, target.queryFilterDtos).append(sortOptions, target.getSortOptions()).
                            append(queryFacetDtos, target.getQueryFacetDtos()).append(locale, target.getLocale()).
                            append(fieldsToResolve, target.getFieldsToResolve()).isEquals();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle()) //--
                .append("expression", expression) //--
                .append("queryFilterDtos", queryFilterDtos) //--
                .append("queryRangeDto", queryRangeDto) //--
                .append("sortOptions", sortOptions) //--
                .append("queryFacetDtos", queryFacetDtos) //--
                .append("locale", locale) //--
                .append("fieldsToResolve", fieldsToResolve) //--
                .append("fieldResolverType", fieldResolverType) //--
                .toString();
    }

    public static QueryDto sortFilter(Expression expression, Locale locale) {
        return new QueryDto(expression, new QueryRangeDto(-1, Integer.MAX_VALUE), null, null, null, locale);
    }
}
