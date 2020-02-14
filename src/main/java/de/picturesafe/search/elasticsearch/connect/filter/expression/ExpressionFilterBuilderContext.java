/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.connect.dto.QueryDto;
import de.picturesafe.search.elasticsearch.connect.filter.ExpressionFilterFactory;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.expression.Expression;

public class ExpressionFilterBuilderContext {
    private final Expression expression;
    private final QueryDto queryDto;
    private final MappingConfiguration mappingConfiguration;
    private final ExpressionFilterFactory initiator;

    public ExpressionFilterBuilderContext(Expression expression, QueryDto queryDto, MappingConfiguration mappingConfiguration,
                                          ExpressionFilterFactory initiator) {
        this.expression = expression;
        this.queryDto = queryDto;
        this.mappingConfiguration = mappingConfiguration;
        this.initiator = initiator;
    }

    public Expression getExpression() {
        return expression;
    }

    public QueryDto getQueryDto() {
        return queryDto;
    }

    public MappingConfiguration getMappingConfiguration() {
        return mappingConfiguration;
    }

    ExpressionFilterFactory getInitiator() {
        return initiator;
    }
}
