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

import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.expression.DayRangeExpression;
import de.picturesafe.search.expression.Expression;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import java.util.Date;

public class DayRangeExpressionFilterBuilder extends AbstractExpressionFilterBuilder implements TimeZoneAware {

    private String timeZone;

    public DayRangeExpressionFilterBuilder(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    protected boolean supportsExpression(Expression expression) {
        return expression instanceof DayRangeExpression;
    }

    @Override
    protected QueryBuilder buildExpressionFilter(ExpressionFilterBuilderContext context) {
        final DayRangeExpression dayRangeExpression = (DayRangeExpression) context.getExpression();
        if (dayRangeExpression.getFromDay() != null || dayRangeExpression.getUntilDay() != null) {
            final String fieldName = dayRangeExpression.getName();
            final RangeQueryBuilder rangeFilterBuilder = QueryBuilders.rangeQuery(fieldName);
            rangeFilterBuilder.gte(ElasticDateUtils.formatIso(dayRangeExpression.getFromDay(), timeZone));
            Date untilDate = dayRangeExpression.getUntilDay();
            if (untilDate != null) {
                untilDate = DateUtils.addDays(untilDate, 1);
                rangeFilterBuilder.lt(ElasticDateUtils.formatIso(untilDate, timeZone));
            }
            return rangeFilterBuilder;
        }
        return null;
    }
}
