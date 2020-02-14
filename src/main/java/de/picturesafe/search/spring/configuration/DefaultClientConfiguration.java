package de.picturesafe.search.spring.configuration;

import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:elasticsearch.properties")
public class DefaultClientConfiguration {

    @Value("${elasticsearch.hosts:localhost:9200}")
    private String elasticsearchHosts;

    @Value("${elasticsearch.sniffer.enabled:false}")
    private boolean snifferEnabled;

    @Bean
    public RestClientConfiguration restClientConfiguration() {
        final RestClientConfiguration rcc = new RestClientConfiguration(elasticsearchHosts);
        rcc.setSnifferEnabled(snifferEnabled);
        return rcc;
    }
}
