package de.picturesafe.search.spring.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
@Import({DefaultClientConfiguration.class, DefaultIndexConfiguration.class, DefaultQueryConfiguration.class, DefaultAggregationConfiguration.class})
public class DefaultElasticConfiguration { }
