/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.spring.configuration;

import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.connect.filter.DefaultExpressionFilterFactory;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.query.FulltextQueryFactory;
import de.picturesafe.search.elasticsearch.connect.query.NestedQueryFactory;
import de.picturesafe.search.elasticsearch.connect.query.OperationExpressionQueryFactory;
import de.picturesafe.search.elasticsearch.connect.query.QueryFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;

import static de.picturesafe.search.elasticsearch.connect.TimeZoneAware.DEFAULT_TIME_ZONE;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
@Import({FulltextQueryFactory.class, NestedQueryFactory.class, OperationExpressionQueryFactory.class})
public class DefaultQueryConfiguration {

    @Value("${elasticsearch.service.time_zone:" + DEFAULT_TIME_ZONE + "}")
    private String elasticsearchTimeZone;

    @Bean
    public String elasticsearchTimeZone() {
        return elasticsearchTimeZone;
    }

    @Bean
    public QueryConfiguration queryConfiguration() {
        return new QueryConfiguration();
    }

    @Bean
    public List<QueryFactory> queryFactories(FulltextQueryFactory fulltextQueryFactory,
                                      OperationExpressionQueryFactory operationExpressionQueryFactory,
                                      NestedQueryFactory nestedQueryFactory) {
        final List<QueryFactory> queryFactories = new ArrayList<>();
        queryFactories.add(fulltextQueryFactory);
        queryFactories.add(operationExpressionQueryFactory);
        queryFactories.add(nestedQueryFactory);
        return queryFactories;
    }

    @Bean
    public List<FilterFactory> filterFactories(QueryConfiguration queryConfiguration, String elasticsearchTimeZone) {

        final List<FilterFactory> filterFactories = new ArrayList<>();
        filterFactories.add(new DefaultExpressionFilterFactory(queryConfiguration, elasticsearchTimeZone));
        return filterFactories;
    }
}
