package de.picturesafe.search.elasticsearch.connect.filter.expression;

import de.picturesafe.search.elasticsearch.connect.filter.ExpressionFilterFactory;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.MustNotExpression;
import de.picturesafe.search.expression.ValueExpression;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MustNotExpressionFilterBuilderTest {

    @Test
    public void test() {
        final QueryBuilder innerFilter = mock(QueryBuilder.class);
        final ExpressionFilterFactory expressionFilterFactory = mock(ExpressionFilterFactory.class);
        doReturn(innerFilter).when(expressionFilterFactory).buildFilter(any(), any(), any());
        final MustNotExpressionFilterBuilder builder = new MustNotExpressionFilterBuilder();

        final Expression expression = new MustNotExpression(new ValueExpression("test", "test"));
        final ExpressionFilterBuilderContext context = new ExpressionFilterBuilderContext(expression, null, null, expressionFilterFactory);
        final QueryBuilder mustNotFilter = builder.buildFilter(context);
        assertNotNull(mustNotFilter);
        assertTrue(mustNotFilter instanceof BoolQueryBuilder);
        assertEquals(1, ((BoolQueryBuilder) mustNotFilter).mustNot().size());
        assertTrue(((BoolQueryBuilder) mustNotFilter).mustNot().contains(innerFilter));
    }

    @Test
    public void testExpressionNotSupported() {
        final MustNotExpressionFilterBuilder builder = new MustNotExpressionFilterBuilder();

        final Expression expression = new ValueExpression("test", "test");
        final ExpressionFilterBuilderContext context = new ExpressionFilterBuilderContext(expression, null, null, null);
        assertNull(builder.buildFilter(context));
    }

    @Test
    public void testInnerFilterNull() {
        final MustNotExpressionFilterBuilder builder = spy(new MustNotExpressionFilterBuilder());
        doReturn(null).when(builder).buildInnerFilter(any(ExpressionFilterBuilderContext.class));

        final Expression expression = new MustNotExpression(new ValueExpression("test", "test"));
        final ExpressionFilterBuilderContext context = new ExpressionFilterBuilderContext(expression, null, null, null);
        assertNull(builder.buildFilter(context));
    }
}
