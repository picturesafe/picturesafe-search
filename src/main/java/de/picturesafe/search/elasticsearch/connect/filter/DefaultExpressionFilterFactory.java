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

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.filter.expression.DayExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.DayRangeExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.InExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.IsNullExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.KeywordExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.MustNotExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.OperationExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.RangeValueExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.expression.ValueExpressionFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.valuepreparation.KeywordValuePreparer;

import java.util.Arrays;
import java.util.Collections;

public class DefaultExpressionFilterFactory extends ExpressionFilterFactory implements TimeZoneAware {

    public DefaultExpressionFilterFactory(QueryConfiguration queryConfig,
                                          String timeZone) {
        super(Arrays.asList(
                new OperationExpressionFilterBuilder(),
                new MustNotExpressionFilterBuilder(),
                new IsNullExpressionFilterBuilder(),
                new InExpressionFilterBuilder(),
                new RangeValueExpressionFilterBuilder(timeZone),
                new DayRangeExpressionFilterBuilder(timeZone),
                new DayExpressionFilterBuilder(timeZone),
                new KeywordExpressionFilterBuilder(),
                new ValueExpressionFilterBuilder(Collections.singletonList(new KeywordValuePreparer()), queryConfig, timeZone)));
    }
}
