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
     * Term aggregation: This is the maximum number of aggregation results that will be resolved for one field. The parameter can be overridden dynamically by
     * the corresponding {@link de.picturesafe.search.parameter.aggregation.TermsAggregation} instance.
     */
    @Value("${elasticsearch.service.aggregation.max_count:" + TermsAggregationBuilderFactory.DEFAULT_MAX_COUNT + "}")
    private int defaultMaxCount;

    /**
     * The max count limit for aggregation buckets
     *
     * Term aggregation: This is the upper limit of the maximum number of aggregation results that will be resolved for one field.
     */
    @Value("${elasticsearch.service.aggregation.max_count_limit:" + TermsAggregationBuilderFactory.DEFAULT_MAX_COUNT_LIMIT + "}")
    private int maxCountLimit;

    /**
     * The shard size factor
     *
     * Term aggregation: The higher the requested size is, the more accurate the results will be, but also, the more expensive
     * it will be to compute the final results The shard_size parameter can be used to minimize the extra work that comes with
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
