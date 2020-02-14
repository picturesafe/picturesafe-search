/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 */

package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.MappingConfiguration;
import de.picturesafe.search.elasticsearch.config.QueryConfiguration;
import de.picturesafe.search.elasticsearch.connect.TimeZoneAware;
import de.picturesafe.search.elasticsearch.connect.filter.InternalFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.InternalNestedFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.InternalPhraseMatchFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.InternalQueryFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.InternalTermFilterBuilder;
import de.picturesafe.search.elasticsearch.connect.filter.valuepreparation.ValuePrepareContext;
import de.picturesafe.search.elasticsearch.connect.filter.valuepreparation.ValuePreparer;
import de.picturesafe.search.elasticsearch.connect.util.ElasticDateUtils;
import de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils;
import de.picturesafe.search.elasticsearch.connect.util.PhraseMatchHelper;
import de.picturesafe.search.expression.KeywordExpression;
import de.picturesafe.search.expression.ValueExpression;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Date;
import java.util.List;

import static de.picturesafe.search.elasticsearch.connect.util.FieldConfigurationUtils.keywordFieldName;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_STARTS_WITH;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.TERM_WILDCARD;

public class ValueExpressionFilterBuilder extends AbstractFieldExpressionFilterBuilder implements TimeZoneAware {

    private final List<ValuePreparer> valuePreparers;
    private final String timeZone;

    private InternalQueryFilterBuilder internalQueryFilterBuilder;
    private InternalTermFilterBuilder internalTermFilterBuilder;
    private InternalNestedFilterBuilder internalNestedFilterBuilder;
    private InternalPhraseMatchFilterBuilder internalPhraseMatchFilterBuilder;

    public ValueExpressionFilterBuilder(List<ValuePreparer> valuePreparers, QueryConfiguration queryConfig, String timeZone) {
        this.valuePreparers = valuePreparers;
        this.timeZone = timeZone;

        internalQueryFilterBuilder = new InternalQueryFilterBuilder(queryConfig);
        internalTermFilterBuilder = new InternalTermFilterBuilder();
        internalNestedFilterBuilder = new InternalNestedFilterBuilder();
        internalPhraseMatchFilterBuilder = new InternalPhraseMatchFilterBuilder();
    }

    @Override
    public QueryBuilder buildFilter(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof ValueExpression) || context.getExpression() instanceof KeywordExpression) {
            return null;
        }

        final ValueExpression expression = (ValueExpression) context.getExpression();
        if (expression.getName().equals(FieldConfiguration.FIELD_NAME_FULLTEXT)) {
            return null;
        }

        final MappingConfiguration mappingConfiguration = context.getMappingConfiguration();
        final String esFieldName
                = FieldConfigurationUtils.getElasticFieldName(mappingConfiguration, expression.getName(), context.getQueryDto().getLocale());
        final FieldConfiguration fieldConfig = FieldConfigurationUtils.fieldConfiguration(mappingConfiguration, esFieldName);
        final ValueExpression.Comparison comparison = expression.getComparison();

        Object value = prepareValue(expression);
        final InternalFilterBuilder internalFilterBuilder = determineInternalFilterBuilder(expression, fieldConfig, value);

        if (value instanceof Date) {
            value = ElasticDateUtils.formatIso((Date) value, timeZone);
        }

        switch (comparison) {
            case EQ:
            case LIKE:
                return internalFilterBuilder.build(esFieldName, value);
            case NOT_EQ:
                return QueryBuilders.boolQuery().mustNot(internalFilterBuilder.build(esFieldName, value));
            case NOT_LIKE:
                return internalFilterBuilder.build(esFieldName, "NOT (" + value + ")");
            case GT:
                return QueryBuilders.rangeQuery(esFieldName).gt(value);
            case LT:
                return QueryBuilders.rangeQuery(esFieldName).lt(value);
            case GE:
                return QueryBuilders.rangeQuery(esFieldName).gte(value);
            case LE:
                return QueryBuilders.rangeQuery(esFieldName).lte(value);
            case TERM_STARTS_WITH:
            case TERM_ENDS_WITH:
            case TERM_WILDCARD:
                Validate.isTrue(fieldConfig.isSortable() || fieldConfig.isAggregatable(),
                        "Field configuration of '" + esFieldName + "' has to be sortable or aggregatable to support comparison: " + comparison);
                final String strValue = (comparison == TERM_WILDCARD)
                        ? String.valueOf(value)
                        : (comparison == TERM_STARTS_WITH)
                                ? value + "*" : "*" + value;
                return QueryBuilders.wildcardQuery(keywordFieldName(fieldConfig, esFieldName), strValue);
            default:
                throw new RuntimeException("Unsupported comparison: " + comparison);
        }
    }

    private InternalFilterBuilder determineInternalFilterBuilder(ValueExpression expression, FieldConfiguration fieldConfig, Object value) {
        InternalFilterBuilder filterBuilder = null;
        if (fieldConfig != null) {
            if (fieldConfig.isNestedObject()) {
                filterBuilder = internalNestedFilterBuilder;
            } else if (fieldConfig.getElasticsearchType().equalsIgnoreCase(ElasticsearchType.BOOLEAN.toString()) && Boolean.FALSE.equals(value)) {
                // Boolean-Feld nicht vorhanden wird als false interpretiert.
                filterBuilder = (k, v) ->
                        new BoolQueryBuilder().should(QueryBuilders.termQuery(k, v)).should(new BoolQueryBuilder().mustNot(new ExistsQueryBuilder(k)));
            }
        }
        if (filterBuilder == null && value instanceof String) {
            if (matchPhrase(expression)) {
                filterBuilder = internalPhraseMatchFilterBuilder;
            } else {
                filterBuilder = internalQueryFilterBuilder;
            }
        }

        if (filterBuilder == null) {
            filterBuilder = internalTermFilterBuilder;
        }

        return filterBuilder;
    }

    private Object prepareValue(ValueExpression expression) {
        final Object value;
        value = expression.getValue();
        final ValuePrepareContext valuePrepareContext = new ValuePrepareContext(value);
        if (!expression.isMatchPhrase()) {
            for (ValuePreparer valuePreparer : valuePreparers) {
                valuePreparer.prepare(valuePrepareContext);
            }
        }

        return valuePrepareContext.getValue();
    }

    private static boolean matchPhrase(ValueExpression expression) {
        return PhraseMatchHelper.matchPhrase((String) expression.getValue()) || expression.isMatchPhrase();
    }

    @Override
    public boolean canHandleSearch(ExpressionFilterBuilderContext context) {
        if (!(context.getExpression() instanceof KeywordExpression) && context.getExpression() instanceof ValueExpression) {
            return hasFieldConfiguration(context);
        } else {
            return false;
        }
    }
}
