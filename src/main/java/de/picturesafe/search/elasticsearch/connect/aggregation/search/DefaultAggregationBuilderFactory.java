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

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.parameter.aggregation.DateHistogramAggregation;
import de.picturesafe.search.parameter.aggregation.DateRangeAggregation;
import de.picturesafe.search.parameter.aggregation.DefaultAggregation;
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.search.aggregations.AggregationBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.picturesafe.search.elasticsearch.connect.util.ElasticAggregationUtils.aggregationName;

public class DefaultAggregationBuilderFactory implements AggregationBuilderFactory<DefaultAggregation> {

    private final AggregationBuilderFactoryRegistry registry;

    public DefaultAggregationBuilderFactory(AggregationBuilderFactoryRegistry registry) {
        this.registry = registry;
    }

    @Override
    public List<AggregationBuilder> create(DefaultAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final FieldConfiguration fieldConfig = mappingConfiguration.getFieldConfiguration(aggregation.getField());
        Validate.notNull(fieldConfig, "Missing field configuration for field '%s', default aggregation cannot be used!", aggregation.getField());
        return fieldConfig.getElasticsearchType().equals(ElasticsearchType.DATE.toString())
                ? createDateAggregationBuilders(aggregation, mappingConfiguration, locale)
                : createTermsAggregationBuilders(aggregation, mappingConfiguration, locale);
    }

    private List<AggregationBuilder> createDateAggregationBuilders(DefaultAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final List<AggregationBuilder> aggregationBuilders = new ArrayList<>();
        final String namePrefix = aggregationName(aggregation) + "-";

        final DateHistogramAggregation dateHistogramAggregation = DateHistogramAggregation.fromDefault(aggregation).name(namePrefix + "histogram");
        final AggregationBuilderFactory<DateHistogramAggregation> dateHistogramFactory = registry.get(DateHistogramAggregation.class);
        if (dateHistogramFactory != null) {
            aggregationBuilders.addAll(dateHistogramFactory.create(dateHistogramAggregation, mappingConfiguration, locale));
        }

        final DateRangeAggregation dateRangeAggregation = DateRangeAggregation.fromDefault(aggregation).name(namePrefix + "ranges");
        final AggregationBuilderFactory<DateRangeAggregation> dateRangeFactory = registry.get(DateRangeAggregation.class);
        if (dateRangeFactory != null) {
            aggregationBuilders.addAll(dateRangeFactory.create(dateRangeAggregation, mappingConfiguration, locale));
        }

        return aggregationBuilders;
    }

    private List<AggregationBuilder> createTermsAggregationBuilders(DefaultAggregation aggregation, MappingConfiguration mappingConfiguration, Locale locale) {
        final TermsAggregation termsAggregation = TermsAggregation.fromDefault(aggregation);
        final AggregationBuilderFactory<TermsAggregation> factory = registry.get(TermsAggregation.class);
        return (factory != null) ? factory.create(termsAggregation, mappingConfiguration, locale) : Collections.emptyList();
    }

    @Override
    public Class<DefaultAggregation> getAggregationType() {
        return DefaultAggregation.class;
    }
}
