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

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.timezone.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.expression.ConditionExpression;
import de.picturesafe.search.expression.DayExpression;
import de.picturesafe.search.expression.Expression;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.Date;

public class DayExpressionFilterBuilder extends AbstractExpressionFilterBuilder implements TimeZoneAware {

    private String timeZone;

    public DayExpressionFilterBuilder(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    protected boolean supportsExpression(Expression expression) {
        return expression instanceof DayExpression;
    }

    @Override
    protected QueryBuilder buildExpressionFilter(ExpressionFilterBuilderContext context) {
        final DayExpression expression = (DayExpression) context.getExpression();
        final String fieldName = expression.getName();
        final ConditionExpression.Comparison comparison = expression.getComparison();
        final RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(fieldName);
        Date day = expression.getDay();
        switch (comparison) {
            case EQ:
                return createEqualsQueryBuilder(fieldName, day, rangeQueryBuilder);
            case NOT_EQ:
                return QueryBuilders.boolQuery().mustNot(createEqualsQueryBuilder(fieldName, day, rangeQueryBuilder));
            case GT:
                day = DateUtils.addDays(day, 1);
                return rangeQueryBuilder.gt(ElasticDateUtils.formatIso(day, timeZone));
            case LT:
                return rangeQueryBuilder.lt(ElasticDateUtils.formatIso(day, timeZone));
            case GE:
                return rangeQueryBuilder.gte(ElasticDateUtils.formatIso(day, timeZone));
            case LE:
                day = DateUtils.addDays(day, 1);
                return rangeQueryBuilder.lt(ElasticDateUtils.formatIso(day, timeZone));
            default:
                throw new RuntimeException("Unsupported comparison " + comparison);
        }
    }

    private BoolQueryBuilder createEqualsQueryBuilder(String mappedFieldName, Date day, RangeQueryBuilder rangeQueryBuilder) {
        final String isoDay = ElasticDateUtils.formatIso(day, timeZone);
        final Date nextDay = DateUtils.addDays(day, 1);
        final String isoNextDay = ElasticDateUtils.formatIso(nextDay, timeZone);
        return QueryBuilders
                .boolQuery()
                .should(rangeQueryBuilder.gte(isoDay).lt(isoNextDay))
                .should(QueryBuilders.termQuery(mappedFieldName, isoDay));
    }
}
