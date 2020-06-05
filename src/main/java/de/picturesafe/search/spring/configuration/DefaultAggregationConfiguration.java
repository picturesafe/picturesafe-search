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

package de.picturesafe.search.spring.configuration;

import de.picturesafe.search.elasticsearch.connect.aggregation.search.AggregationBuilderFactoryRegistry;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.DateHistogramAggregationBuilderFactory;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.DateRangeAggregationBuilderFactory;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.DefaultAggregationBuilderFactory;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.TermsAggregationBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
public class DefaultAggregationConfiguration {

    /**
     * The default maximum count for aggregation buckets
     *
     * Terms aggregation: This is the maximum number of aggregation results that will be resolved for one field. The parameter can be overridden dynamically by
     * the corresponding {@link de.picturesafe.search.parameter.aggregation.TermsAggregation} instance.
     */
    @Value("${elasticsearch.service.aggregation.max_count:" + TermsAggregationBuilderFactory.DEFAULT_MAX_COUNT + "}")
    private int defaultMaxCount;

    /**
     * The max count limit for aggregation buckets
     *
     * Terms aggregation: This is the upper limit of the maximum number of aggregation results that will be resolved for one field.
     */
    @Value("${elasticsearch.service.aggregation.max_count_limit:" + TermsAggregationBuilderFactory.DEFAULT_MAX_COUNT_LIMIT + "}")
    private int maxCountLimit;

    /**
     * The shard size factor
     *
     * Terms aggregation: The higher the requested size is, the more accurate the results will be, but also, the more expensive
     * it will be to compute the final results. The shard_size parameter can be used to minimize the extra work that comes with
     * bigger requested size. When defined, it will determine how many terms the coordinating node will request from each shard.
     * Once all the shards responded, the coordinating node will then reduce them to a final result which
     * will be based on the size parameter - this way, one can increase the accuracy of the returned terms and avoid the overhead
     * of streaming a big list of buckets back to the client.
     *
     * shard_size = maxCount * shardSizeFactor
     */
    @Value("${elasticsearch.service.aggregation.shard_size_factor:" + TermsAggregationBuilderFactory.DEFAULT_SHARD_SIZE_FACTOR + "}")
    private int shardSizeFactor;

    @Bean
    public AggregationBuilderFactoryRegistry aggregationBuilderFactoryRegistry() {
        final AggregationBuilderFactoryRegistry registry = new AggregationBuilderFactoryRegistry(
                new TermsAggregationBuilderFactory(defaultMaxCount, maxCountLimit, shardSizeFactor),
                new DateRangeAggregationBuilderFactory(),
                new DateHistogramAggregationBuilderFactory()
        );
        registry.put(new DefaultAggregationBuilderFactory(registry));
        return registry;
    }
}
