/*
 * Copyright 2020 picturesafe media/data/bank GmbH
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
