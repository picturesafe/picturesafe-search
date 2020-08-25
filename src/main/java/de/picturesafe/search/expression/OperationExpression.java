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

package de.picturesafe.search.expression;

import de.picturesafe.search.expression.internal.EmptyExpression;
import de.picturesafe.search.expression.internal.FalseExpression;
import de.picturesafe.search.expression.internal.TrueExpression;
import de.picturesafe.search.util.ArrayUtils;
import de.picturesafe.search.util.logging.CustomJsonToStringStyle;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import static de.picturesafe.search.expression.ConditionExpression.Comparison.GE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.GT;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LE;
import static de.picturesafe.search.expression.ConditionExpression.Comparison.LT;

/**
 * Expression to combine several expressions via a logical operation.
 */
public class OperationExpression extends AbstractExpression {

    public enum Operator {
        AND, OR
    }

    private Operator operator;
    private final List<Expression> operands = new ArrayList<>();

    public OperationExpression(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public List<Expression> getOperands() {
        return operands;
    }

    public OperationExpression add(Expression... expressions) {
        CollectionUtils.addAll(operands, expressions);
        return this;
    }

    public OperationExpression addAll(Collection<Expression> expressions) {
        operands.addAll(expressions);
        return this;
    }

    public static OperationExpression and() {
        return new OperationExpression(Operator.AND);
    }

    public static OperationExpression and(Expression... expressions) {
        return new OperationExpression(Operator.AND).add(expressions);
    }

    public static OperationExpression and(Collection<Expression> expressions) {
        return new OperationExpression(Operator.AND).addAll(expressions);
    }

    public static OperationExpression or() {
        return new OperationExpression(Operator.OR);
    }

    public static OperationExpression or(Expression... expressions) {
        return new OperationExpression(Operator.OR).add(expressions);
    }

    public static OperationExpression or(Collection<Expression> expressions) {
        return new OperationExpression(Operator.OR).addAll(expressions);
    }

    @Override
    public Expression optimize() {
        if (operator == null) {
            throw new IllegalStateException("The argument 'operator' must not be null!");
        } else {
            final Expression ret;
            if (CollectionUtils.isEmpty(operands)) {
                ret = new EmptyExpression();
            } else {
                final List<Expression> optimizedOperands = optimizeOperands();

                if (optimizedOperands.isEmpty()) {
                    ret = new EmptyExpression();
                } else if (optimizedOperands.size() == 1) {
                    ret = optimizedOperands.get(0);
                } else {
                    ret = new OperationExpression(operator).addAll(optimizedOperands);
                }
            }
            return ret;
        }
    }

    private enum OptimizationStrategy {
        UNION,
        INTERSECT,
        COMPLEMENT,
        NONE
    }

    private List<Expression> optimizeOperands() {
        Validate.notNull(operator, "The argument 'operator' must not be null!");
        Validate.notNull(operands, "The argument 'operands' must not be null!");

        final ExpressionsHolder expressionsHolder = new ExpressionsHolder();
        final List<Expression> ret = new ArrayList<>(operands.size());

        final Queue<Expression> queuedOperands = new LinkedList<>();
        enqueueOperands(queuedOperands, operands);

        while (!queuedOperands.isEmpty()) {
            final Expression operand = queuedOperands.poll();
            if (operand != null) {
                final Expression optimizedOperand = operand.optimize();

                if (optimizedOperand != null) {
                    if (optimizedOperand instanceof TrueExpression) {
                        if (operator == Operator.OR) {
                            // If several operands are connected with OR, and one of them is always true,
                            // then the whole expression is always true too.
                            ret.clear();
                            ret.add(optimizedOperand);
                            break;
                        } else {
                            // AND: Add TRUE operand only if there are other operands
                            if (queuedOperands.isEmpty() && ret.isEmpty() && expressionsHolder.isEmpty()) {
                                ret.add(optimizedOperand);
                            }
                        }
                    } else if (optimizedOperand instanceof FalseExpression) {
                        if (operator == Operator.AND) {
                            // If several operands are connected with OR, and one of them is always false,
                            // then the whole expression is always false too.
                            ret.clear();
                            ret.add(optimizedOperand);
                            break;
                        } else {
                            // OR: Add FALSE operand only if there are other operands
                            if (queuedOperands.isEmpty() && ret.isEmpty() && expressionsHolder.isEmpty()) {
                                ret.add(optimizedOperand);
                            }
                        }
                    } else if (optimizedOperand.getClass().equals(OperationExpression.class)) {
                        final OperationExpression optimizedOperationExpressionOperand = (OperationExpression) optimizedOperand;
                        if (operator.equals(optimizedOperationExpressionOperand.getOperator())) {
                            // Flatten nested ANDs/ORs for merging InExpressions or RangeValueExpressions
                            queuedOperands.addAll(optimizedOperationExpressionOperand.getOperands());
                        } else {
                            ret.add(optimizedOperationExpressionOperand);
                        }
                    } else if (optimizedOperand instanceof InExpression) {
                        optimizeInExpression((InExpression) optimizedOperand, expressionsHolder, ret);
                    } else if (optimizedOperand instanceof MustNotExpression) {
                        optimizeMustNotExpression((MustNotExpression) optimizedOperand, expressionsHolder, ret);
                    } else if (operator == Operator.AND &&  optimizedOperand.getClass().equals(ValueExpression.class)) {
                        optimizeValueExpression((ValueExpression) optimizedOperand, expressionsHolder, ret);
                    } else if (operator == Operator.AND &&  optimizedOperand.getClass().equals(DayExpression.class)) {
                        optimizeDayExpression((DayExpression) optimizedOperand, expressionsHolder, ret);
                    } else if (!(optimizedOperand instanceof EmptyExpression)) {
                        ret.add(optimizedOperand);
                    }
                }
            }
        }

        ret.addAll(expressionsHolder.getCollectedInExpressions().values());
        ret.addAll(expressionsHolder.getCollectedNotInExpressions().values());

        mergeRangeValueExpressions(expressionsHolder, ret);
        mergeDayRangeExpressions(expressionsHolder, ret);

        return ret;
    }

    private static String normalizeFieldname(String fieldname) {
        return StringUtils.defaultString(fieldname).toLowerCase(Locale.ENGLISH).trim();
    }

    private static void enqueueOperands(Queue<Expression> queue, Collection<Expression> operands) {
        Validate.notNull(queue, "The argument 'queue' must not be null!");

        if (operands != null) {
            for (Expression operand : operands) {
                if (!(operand instanceof EmptyExpression)) {
                    queue.add(operand);
                }
            }
        }
    }

    private void optimizeInExpression(InExpression inExpression,
                                      ExpressionsHolder expressionsHolder,
                                      List<Expression> ret) {

        final String name = inExpression.getName();
        final InExpression optimizedExpression = expressionsHolder.getCollectedInExpressions().get(name);

        // intersection for AND, union for OR
        if (optimizedExpression == null) {
            expressionsHolder.getCollectedInExpressions().put(name, inExpression);
        } else {
            final OptimizationStrategy strategy = getStrategy(operator, false);

            if (strategy != OptimizationStrategy.NONE) {
                final Object[] values = optimize(optimizedExpression.getValues(), inExpression.getValues(), strategy);
                optimizedExpression.setValues(values);
            } else {
                ret.add(inExpression);
            }
        }
    }

    private void optimizeMustNotExpression(MustNotExpression mustNotExpression,
                                           ExpressionsHolder expressionsHolder,
                                           List<Expression> ret) {

        if (mustNotExpression.getExpression() instanceof InExpression) {
            final InExpression notInExpression = (InExpression) mustNotExpression.getExpression();
            final String name = notInExpression.getName();
            final MustNotExpression collectedExpression = expressionsHolder.getCollectedNotInExpressions().getOrDefault(name, null);

            // intersection for OR, union for AND
            if (collectedExpression == null) {
                expressionsHolder.getCollectedNotInExpressions().put(name, mustNotExpression);
            } else {
                final OptimizationStrategy strategy = getStrategy(operator, true);

                if (strategy != OptimizationStrategy.NONE) {
                    final InExpression collectedNotInExpression = (InExpression) collectedExpression.getExpression();
                    final Object[] values = optimize(collectedNotInExpression.getValues(), notInExpression.getValues(), strategy);
                    collectedNotInExpression.setValues(values);
                } else {
                    ret.add(mustNotExpression);
                }
            }
        } else {
            ret.add(mustNotExpression);
        }
    }

    private void optimizeValueExpression(ValueExpression valueExpression,
                                         ExpressionsHolder expressionsHolder,
                                         List<Expression> ret) {

        if ((valueExpression.getComparison() == GE || valueExpression.getComparison() == GT)
                && (valueExpression.getValue() == null || valueExpression.getValue() instanceof Date)) {
            final String normalizedFieldname = normalizeFieldname(valueExpression.getName());
            final ValueExpression previousRememberedValue = expressionsHolder.getCollectedGreaterExpressions()
                    .put(normalizedFieldname, valueExpression);
            if (previousRememberedValue != null) {
                throw new IllegalStateException("Multiple ValueExpression with same name '"
                        + normalizedFieldname + "' and operator 'GE' or 'GT' can not be optimized to "
                        + "RangeValueExpressions.");
            }
        } else if ((valueExpression.getComparison() == LE || valueExpression.getComparison() == LT)
                && (valueExpression.getValue() == null || valueExpression.getValue() instanceof Date)) {
            final String normalizedFieldname = normalizeFieldname(valueExpression.getName());
            final ValueExpression previousRememberedValue = expressionsHolder.getCollectedLesserExpressions()
                    .put(normalizedFieldname, valueExpression);
            if (previousRememberedValue != null) {
                throw new IllegalStateException("Multiple ValueExpression with same name '"
                        + normalizedFieldname + "' and operator 'LE' or 'LT' can not be optimized to "
                        + "RangeValueExpressions.");
            }
        } else {
            ret.add(valueExpression);
        }
    }

    private void optimizeDayExpression(DayExpression dayExpression,
                                       ExpressionsHolder expressionsHolder,
                                       List<Expression> ret) {

        if (dayExpression.getComparison() == GE || dayExpression.getComparison() == GT) {
            final String normalizedFieldname = normalizeFieldname(dayExpression.getName());
            final DayExpression previousRememberedValue = expressionsHolder.getCollectedDayGreaterExpressions()
                    .put(normalizedFieldname, dayExpression);
            if (previousRememberedValue != null) {
                throw new IllegalStateException("Multiple DayExpression with same name '"
                        + normalizedFieldname + "' and operator 'GE' or 'GT' can not be optimized to "
                        + "DayRangeExpressions.");
            }
        } else if (dayExpression.getComparison() == LE || dayExpression.getComparison() == LT) {
            final String normalizedFieldname = normalizeFieldname(dayExpression.getName());
            final DayExpression previousRememberedValue = expressionsHolder.getCollectedDayLesserExpressions()
                    .put(normalizedFieldname, dayExpression);
            if (previousRememberedValue != null) {
                throw new IllegalStateException("Multiple DayExpression with same name '"
                        + normalizedFieldname + "' and operator 'LE' or 'LT' can not be optimized to "
                        + "DayRangeExpressions.");
            }
        } else {
            ret.add(dayExpression);
        }
    }

    private void mergeRangeValueExpressions(ExpressionsHolder expressionsHolder, List<Expression> ret) {

        // Merge GE expressions and LE expressions with the same field name into RangeValueExpression
        for (Entry<String, ValueExpression> entry : expressionsHolder.getCollectedGreaterExpressions().entrySet()) {
            final ValueExpression matchingLesserExpression = expressionsHolder.getCollectedLesserExpressions()
                    .remove(entry.getKey());
            if (matchingLesserExpression != null) {
                final ValueExpression matchingGreaterExpression = entry.getValue();

                final RangeValueExpression rangeValueExpression = new RangeValueExpression();
                rangeValueExpression.setName(matchingLesserExpression.getName());

                final Date fromValue;
                if (matchingGreaterExpression.getValue() == null) {
                    fromValue = null;
                } else if (matchingGreaterExpression.getComparison() == GE) {
                    fromValue = (Date) matchingGreaterExpression.getValue();
                } else if (matchingGreaterExpression.getComparison() == GT) {
                    fromValue = DateUtils.addDays((Date) matchingGreaterExpression.getValue(), 1);
                } else {
                    throw new IllegalStateException("Unexpected Expression: " + matchingGreaterExpression);
                }
                rangeValueExpression.setMinValue(fromValue);

                final Date untilValue;
                if (matchingLesserExpression.getValue() == null) {
                    untilValue = null;
                } else if (matchingLesserExpression.getComparison() == LE) {
                    untilValue = (Date) matchingLesserExpression.getValue();
                } else if (matchingLesserExpression.getComparison() == LT) {
                    untilValue = DateUtils.addDays((Date) matchingLesserExpression.getValue(), -1);
                } else {
                    throw new IllegalStateException("Unexpected Expression: " + matchingGreaterExpression);
                }
                rangeValueExpression.setMaxValue(untilValue);

                final Expression optimizedExpression = rangeValueExpression.optimize();
                ret.add(optimizedExpression);
            } else {
                ret.add(entry.getValue());
            }
        }
        ret.addAll(expressionsHolder.getCollectedLesserExpressions().values());
    }

    private void mergeDayRangeExpressions(ExpressionsHolder expressionsHolder, List<Expression> ret) {

        // Merge GE expressions and LE expressions with the same field name into DayRangeExpression
        for (Entry<String, DayExpression> entry : expressionsHolder.getCollectedDayGreaterExpressions().entrySet()) {
            final DayExpression matchingLesserExpression = expressionsHolder.getCollectedDayLesserExpressions()
                    .remove(entry.getKey());
            if (matchingLesserExpression != null) {
                final DayExpression matchingGreaterExpression = entry.getValue();

                final DayRangeExpression dayRangeExpression = new DayRangeExpression();
                dayRangeExpression.setName(matchingLesserExpression.getName());

                final Date fromValue;
                if (matchingGreaterExpression.getDay() == null) {
                    fromValue = null;
                } else if (matchingGreaterExpression.getComparison() == GE) {
                    fromValue = matchingGreaterExpression.getDay();
                } else if (matchingGreaterExpression.getComparison() == GT) {
                    fromValue = DateUtils.addDays(matchingGreaterExpression.getDay(), 1);
                } else {
                    throw new IllegalStateException("Unexpected Expression: " + matchingGreaterExpression);
                }
                dayRangeExpression.setFromDay(fromValue);

                final Date untilValue;
                if (matchingLesserExpression.getDay() == null) {
                    untilValue = null;
                } else if (matchingLesserExpression.getComparison() == LE) {
                    untilValue = matchingLesserExpression.getDay();
                } else if (matchingLesserExpression.getComparison() == LT) {
                    untilValue = DateUtils.addDays(matchingLesserExpression.getDay(), -1);
                } else {
                    throw new IllegalStateException("Unexpected Expression: " + matchingGreaterExpression);
                }
                dayRangeExpression.setUntilDay(untilValue);

                final Expression optimizedExpression = dayRangeExpression.optimize();
                ret.add(optimizedExpression);
            } else {
                ret.add(entry.getValue());
            }
        }
        ret.addAll(expressionsHolder.getCollectedDayLesserExpressions().values());
    }

    private static OptimizationStrategy getStrategy(OperationExpression.Operator operator, boolean negate) {
        return (operator == Operator.AND ^ negate) ? OptimizationStrategy.INTERSECT : OptimizationStrategy.UNION;
    }

    private static Object[] optimize(Object[] a, Object[] b, OptimizationStrategy strategy) {
        switch (strategy) {
            case INTERSECT:
                return ArrayUtils.intersect(a, b);
            case UNION:
                return ArrayUtils.union(a, b);
            case COMPLEMENT:
                return ArrayUtils.complement(a, b);
            default:
                throw new RuntimeException("Unsupported optimization strategy: " + strategy);
        }
    }

    private static class ExpressionsHolder {
        private Map<String, InExpression> collectedInExpressions = new HashMap<>();
        private Map<String, MustNotExpression> collectedNotInExpressions = new HashMap<>();
        private Map<String, ValueExpression> collectedGreaterExpressions = new HashMap<>();
        private Map<String, ValueExpression> collectedLesserExpressions = new HashMap<>();
        private Map<String, DayExpression> collectedDayGreaterExpressions = new HashMap<>();
        private Map<String, DayExpression> collectedDayLesserExpressions = new HashMap<>();

        Map<String, InExpression> getCollectedInExpressions() {
            return collectedInExpressions;
        }

        Map<String, MustNotExpression> getCollectedNotInExpressions() {
            return collectedNotInExpressions;
        }

        Map<String, ValueExpression> getCollectedGreaterExpressions() {
            return collectedGreaterExpressions;
        }

        Map<String, ValueExpression> getCollectedLesserExpressions() {
            return collectedLesserExpressions;
        }

        Map<String, DayExpression> getCollectedDayGreaterExpressions() {
            return collectedDayGreaterExpressions;
        }

        Map<String, DayExpression> getCollectedDayLesserExpressions() {
            return collectedDayLesserExpressions;
        }

        boolean isEmpty() {
            return getCollectedInExpressions().isEmpty()
                    && getCollectedNotInExpressions().isEmpty()
                    && getCollectedGreaterExpressions().isEmpty()
                    && getCollectedLesserExpressions().isEmpty()
                    && getCollectedDayGreaterExpressions().isEmpty()
                    && getCollectedDayLesserExpressions().isEmpty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OperationExpression that = (OperationExpression) o;
        return new EqualsBuilder()
                .append(operator, that.operator)
                .append(operands, that.operands)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(operator)
                .append(operands)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new CustomJsonToStringStyle())
                .append("operator", operator)
                .append("operands", operands)
                .toString();
    }
}