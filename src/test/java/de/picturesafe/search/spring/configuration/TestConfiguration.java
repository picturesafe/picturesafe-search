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

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.LanguageSortConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.SuggestFieldConfiguration;
import de.picturesafe.search.elasticsearch.connect.ElasticsearchAdmin;
import de.picturesafe.search.elasticsearch.connect.aggregation.resolve.FacetResolver;
import de.picturesafe.search.elasticsearch.connect.filter.DefaultExpressionFilterFactory;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchAdminImpl;
import de.picturesafe.search.elasticsearch.connect.mock.FacetResolverMock;
import de.picturesafe.search.elasticsearch.connect.support.IndexSetup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.config.FieldConfiguration.FIELD_NAME_SUGGEST;

@Configuration
@Import({ DefaultClientConfiguration.class, DefaultIndexConfiguration.class, DefaultQueryConfiguration.class, DefaultAggregationConfiguration.class,
        ElasticsearchAdminImpl.class})
@PropertySource(value = "classpath:${ES_PROPERTIES_CLASSPATH:elasticsearch.properties}", ignoreResourceNotFound = true)
public class TestConfiguration {

    @Bean
    protected MappingConfiguration mappingConfiguration() {
        return new MappingConfiguration(fieldConfigurations(), languageSortConfigurations());
    }

    @Bean
    protected List<FilterFactory> filterFactories(QueryConfiguration queryConfiguration, String elasticsearchTimeZone) {
        final List<FilterFactory> filterFactories = new ArrayList<>();
        filterFactories.add(new DefaultExpressionFilterFactory(queryConfiguration, elasticsearchTimeZone));
        return filterFactories;
    }

    @Bean
    protected FacetResolver facetResolver() {
        return new FacetResolverMock();
    }

    @Bean
    protected IndexSetup indexSetup(MappingConfiguration mappingConfiguration,
                          IndexPresetConfiguration indexPresetConfiguration,
                          ElasticsearchAdmin elasticsearchAdmin) {
        return new IndexSetup(mappingConfiguration, indexPresetConfiguration, elasticsearchAdmin);
    }

    protected List<LanguageSortConfiguration> languageSortConfigurations() {
        final List<LanguageSortConfiguration> languageSortConfigurations = new ArrayList<>();
        languageSortConfigurations.add(new LanguageSortConfiguration(Locale.GERMANY));
        languageSortConfigurations.add(new LanguageSortConfiguration(Locale.UK));
        return languageSortConfigurations;
    }

    protected List<FieldConfiguration> fieldConfigurations() {
        final List<FieldConfiguration> testFields = new ArrayList<>();
        testFields.add(FieldConfiguration.ID_FIELD);
        testFields.add(FieldConfiguration.FULLTEXT_FIELD);
        testFields.add(StandardFieldConfiguration.builder(
                "title", ElasticsearchType.TEXT).copyToFulltext(true).aggregatable(true).sortable(true).multilingual(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "caption", ElasticsearchType.TEXT).copyToFulltext(true).aggregatable(true).copyToSuggest(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "location", ElasticsearchType.TEXT).copyToFulltext(true).aggregatable(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "createDate", ElasticsearchType.DATE).build());
        testFields.add(StandardFieldConfiguration.builder(
                "keyword", ElasticsearchType.TEXT).copyToFulltext(true).aggregatable(true).copyToSuggest(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "keywordField", ElasticsearchType.KEYWORD).build());
        testFields.add(StandardFieldConfiguration.builder(
                "facetResolved", ElasticsearchType.TEXT).copyToFulltext(true).sortable(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "treePaths", ElasticsearchType.TEXT).build());
        testFields.add(StandardFieldConfiguration.builder(
                "released", ElasticsearchType.BOOLEAN).aggregatable(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "facetDate", ElasticsearchType.DATE).aggregatable(true).build());
        testFields.add(StandardFieldConfiguration.builder(
                "objectField", ElasticsearchType.OBJECT).withoutIndexing().build());
        testFields.add(StandardFieldConfiguration.builder(
                "numbers", ElasticsearchType.INTEGER).build());
        testFields.add(referenceWithSortFieldConfiguration());
        testFields.add(SuggestFieldConfiguration.name(FIELD_NAME_SUGGEST).additionalParameter("max_input_length", 100));
        return testFields;
    }

    protected FieldConfiguration referenceWithSortFieldConfiguration() {
        final List<StandardFieldConfiguration> nestedFields = new ArrayList<>();
        nestedFields.add(StandardFieldConfiguration.builder("targetId", ElasticsearchType.INTEGER).build());
        nestedFields.add(StandardFieldConfiguration.builder("sortOrder", ElasticsearchType.LONG).sortable(true).build());
        nestedFields.add(StandardFieldConfiguration.builder("linkingTime", ElasticsearchType.LONG).sortable(true).build());
        nestedFields.add(StandardFieldConfiguration.builder("note", ElasticsearchType.TEXT).sortable(true).build());
        return StandardFieldConfiguration.builder("referenceWithSort", ElasticsearchType.NESTED)
                .nestedFields(nestedFields)
                .build();
    }
}
