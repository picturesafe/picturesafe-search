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

package de.picturesafe.search.elasticsearch.connect.aggregation.search;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticAggregationUtils.aggregationName;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.getElasticFieldName;
import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.keywordFieldName;

public class TermsAggregationBuilderFactory implements AggregationBuilderFactory<TermsAggregation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TermsAggregationBuilderFactory.class);

    public static final int DEFAULT_MAX_COUNT = 10;
    public static final int DEFAULT_MAX_COUNT_LIMIT = 20;
    public static final int DEFAULT_SHARD_SIZE_FACTOR = 5;

    private int defaultMaxCount = DEFAULT_MAX_COUNT;
    private int maxCountLimit = DEFAULT_MAX_COUNT_LIMIT;
    private int shardSizeFactor = DEFAULT_SHARD_SIZE_FACTOR;

    public TermsAggregationBuilderFactory() {
    }

    public TermsAggregationBuilderFactory(int defaultMaxCount, int maxCountLimit, int shardSizeFactor) {
        this.defaultMaxCount = defaultMaxCount;
        this.maxCountLimit = maxCountLimit;
        this.shardSizeFactor = shardSizeFactor;
    }

    @Override
    public List<AggregationBuilder> create(TermsAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(aggregationName(aggregation));

        final String fieldName = aggregation.getField();
        final FieldConfiguration fieldConfig = mappingConfiguration.getFieldConfiguration(fieldName);
        if (fieldConfig == null) {
            LOGGER.warn("Missing field configuration for field '{}', aggregations will not work for text fields!", fieldName);
        }

        String aggFieldName = getElasticFieldName(mappingConfiguration, fieldName, locale);
        aggFieldName = keywordFieldName(fieldConfig, aggFieldName);
        int size = Math.min(aggregation.getMaxCount(), maxCountLimit);
        if (size <= 0) {
            size = defaultMaxCount;
        }
        termsBuilder.field(aggFieldName)
                .size(size)
                .shardSize(size * shardSizeFactor)
                .order(order(aggregation))
                .minDocCount(aggregation.getMinDocCount());
        return Collections.singletonList(termsBuilder);
    }

    private BucketOrder order(TermsAggregation aggregation) {
        switch (aggregation.getOrder()) {
            case KEY_ASC:
                return BucketOrder.key(true);
            case KEY_DESC:
                return BucketOrder.key(false);
            default:
                return BucketOrder.count(false);
        }
    }

    @Override
    public Class<TermsAggregation> getAggregationType() {
        return TermsAggregation.class;
    }
}
