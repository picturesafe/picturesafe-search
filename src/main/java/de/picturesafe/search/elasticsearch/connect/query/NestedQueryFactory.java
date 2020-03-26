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

package de.picturesafe.search.elasticsearch.connect.query;

import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactoryContext;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.picturesafe.search.elasticsearch.connect.filter.util.FilterFactoryUtils.createFilter;

@Component
public class NestedQueryFactory implements QueryFactory {

    private final List<FilterFactory> filterFactories;

    @Autowired
    public NestedQueryFactory(List<FilterFactory> filterFactories) {
        this.filterFactories = filterFactories;
    }

    @Override
    public boolean supports(QueryDto queryDto) {
        final Expression expression = queryDto.getExpression();
        return expression instanceof ValueExpression && !((ValueExpression) expression).getName().equals(FieldConfiguration.FIELD_NAME_FULLTEXT);
    }

    @Override
    public QueryBuilder create(QueryFactoryCaller caller, QueryDto queryDto, MappingConfiguration mappingConfiguration) {
        final ValueExpression valueExpression = (ValueExpression) queryDto.getExpression();
        final String fieldName = valueExpression.getName();
        final FieldConfiguration fieldConfiguration = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, fieldName);
        if (fieldConfiguration != null && fieldConfiguration.isNestedObject()) {
            final String objectPath = FieldConfigurationUtils.rootFieldName(fieldConfiguration);
            return QueryBuilders.nestedQuery(objectPath,
                    QueryBuilders.boolQuery().filter(createFilter(filterFactories, queryDto, new FilterFactoryContext(mappingConfiguration, true))),
                    ScoreMode.Total);
        }

        return null;
    }
}
