package com.realdolmen.service;

import com.realdolmen.TestMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportsQueryParserTest {

    @InjectMocks
    private ReportsQueryBuilder queryBuilder = new ReportsQueryBuilder();

    private ReportsQueryBuilder.ReportsQueryParser queryParser = queryBuilder.new ReportsQueryParser();

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder builder;

    @Mock
    private Root root;

    @Mock
    private SetJoin join;

    @Before
    public void setUp() throws Exception {
        TestMode.enableTestMode();
        when(em.getCriteriaBuilder()).thenReturn(builder);
        when(root.joinSet(anyString())).thenReturn(join);
        when(join.get(anyString())).thenThrow(new IllegalArgumentException());
    }

    @Test
    public void testTokenStreamReturnsEmptyStreamOnNull() throws Exception {
        Stream<String> tokens = queryParser.tokenStream(null, "[a-z]");
        assertEquals(0, tokens.collect(Collectors.toList()).size());
        tokens = queryParser.tokenStream("id", null);
        assertEquals(0, tokens.collect(Collectors.toList()).size());
    }

    @Test
    public void testTokenStreamCreatesStreamContainingEveryMatchingGroup() throws Exception {
        List<String> tokens = queryParser.tokenStream("id firstName", "[a-zA-Z]+").collect(Collectors.toList());
        assertEquals(2, tokens.size());
        assertEquals("id", tokens.get(0));
        assertEquals("firstName", tokens.get(1));
    }

    @Test
    public void testTokenListReturnsEmptyStreamOnNull() throws Exception {
        List<String> tokens = queryParser.tokenList(null, "[a-z]");
        assertEquals(0, tokens.size());
        tokens = queryParser.tokenList("id", null);
        assertEquals(0, tokens.size());
    }

    @Test
    public void testTokenListCreatesListContainingEveryMatchingGroup() throws Exception {
        List<String> tokens = queryParser.tokenList("id firstName", "[a-zA-Z]+");
        assertEquals(2, tokens.size());
        assertEquals("id", tokens.get(0));
        assertEquals("firstName", tokens.get(1));
    }
    
    @Test
    public void testCreatePredicatesReturnsEmptyStreamOnNull() throws Exception {
        List<Predicate> predicateList = queryParser.predicateStream(root, null).collect(Collectors.toList());
        assertEquals(0, predicateList.size());
    }

    @Test
    public void testCreatePredicateSplitsSelectionWithOrOperator() throws Exception {
        Predicate or = mock(Predicate.class);
        when(builder.or(anyVararg())).thenReturn(or);
        when(builder.or(any(), any())).thenReturn(or);
        assertEquals("predicate is the OR-predicate", or, testPredicate("id:1 | id:2"));
    }

    @Test
    public void testCreatePredicateChecksEqualsOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.equal(any(), any(Object.class))).thenReturn(predicate);
        assertEquals("predicate is the equal predicate", predicate, testPredicate("id=1"));
    }

    @Test
    public void testCreatePredicateChecksNotEqualsOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.notEqual(any(), any(Object.class))).thenReturn(predicate);
        Path path = mock(Path.class);
        Expression<Class> pathTypeExpression = mock(Expression.class);
        when(root.get("id")).thenReturn(path);
        when(path.type()).thenReturn(pathTypeExpression);
        when(pathTypeExpression.getJavaType()).thenReturn(null);
        assertEquals("predicate is the not equal predicate", predicate, testPredicate("id!=1"));
    }

    @Test
    public void testCreatePredicateChecksLowerThanOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.lt(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.lt(any(), any(Number.class))).thenReturn(predicate);
        assertEquals("predicate is the lower than predicate", predicate, testPredicate("id<1"));
    }

    @Test
    public void testCreatePredicateChecksLowerThanOrEqualToOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.lessThanOrEqualTo(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.lessThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        assertEquals("predicate is the lower than or equal to predicate", predicate, testPredicate("id<=1"));
    }

    @Test
    public void testCreatePredicateChecksGreaterThanOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.gt(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.gt(any(), any(Number.class))).thenReturn(predicate);
        assertEquals("predicate is the greater than predicate", predicate, testPredicate("id>1"));
    }

    @Test
    public void testCreatePredicateChecksGreaterThanOrEqualToOperator() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.greaterThanOrEqualTo(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.greaterThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        assertEquals("predicate is the greater than predicate", predicate, testPredicate("id>=1"));
    }

    @Test
    public void testCreatePredicateChecksEqualsOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.equal(any(), any(Object.class))).thenReturn(predicate);
        Path expression = mock(Path.class);
        when(root.get("id")).thenReturn(expression);
        assertEquals("predicate is the equal predicate", predicate, testPredicate("id.size=1"));
        verify(em.getCriteriaBuilder()).size(expression);
    }

    @Test
    public void testCreatePredicateChecksNotEqualsOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        when(builder.notEqual(any(), any(Object.class))).thenReturn(predicate);
        Path path = mock(Path.class);
        Expression<Class> pathTypeExpression = mock(Expression.class);
        when(root.get("id")).thenReturn(path);
        when(path.type()).thenReturn(pathTypeExpression);
        when(pathTypeExpression.getJavaType()).thenReturn(null);
        assertEquals("predicate is the not equal predicate", predicate, testPredicate("id.size!=1"));
        verify(em.getCriteriaBuilder()).size(any(Expression.class));
    }

    @Test
    public void testCreatePredicateChecksLowerThanOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        Path expression = mock(Path.class);
        when(root.get("id")).thenReturn(expression);
        when(builder.lt(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.lt(any(), any(Number.class))).thenReturn(predicate);
        assertEquals("predicate is the lower than predicate", predicate, testPredicate("id.size<1"));
        verify(em.getCriteriaBuilder()).size(expression);
    }

    @Test
    public void testCreatePredicateChecksLowerThanOrEqualToOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        Path expression = mock(Path.class);
        when(root.get("id")).thenReturn(expression);
        when(builder.lessThanOrEqualTo(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.lessThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        assertEquals("predicate is the lower than or equal to predicate", predicate, testPredicate("id.size<=1"));
        verify(em.getCriteriaBuilder()).size(expression);
    }

    @Test
    public void testCreatePredicateChecksGreaterThanOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        Path expression = mock(Path.class);
        when(root.get("id")).thenReturn(expression);
        when(builder.gt(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.gt(any(), any(Number.class))).thenReturn(predicate);
        assertEquals("predicate is the greater than predicate", predicate, testPredicate("id.size>1"));
        verify(em.getCriteriaBuilder()).size(expression);
    }

    @Test
    public void testCreatePredicateChecksGreaterThanOrEqualToOperatorWithPropertyExpression() throws Exception {
        Predicate predicate = mock(Predicate.class);
        Path expression = mock(Path.class);
        when(root.get("id")).thenReturn(expression);
        when(builder.greaterThanOrEqualTo(any(), any(Expression.class))).thenReturn(predicate);
        when(builder.greaterThanOrEqualTo(any(), any(Double.class))).thenReturn(predicate);
        assertEquals("predicate is the greater than predicate", predicate, testPredicate("id.size>=1"));
        verify(em.getCriteriaBuilder()).size(expression);
    }

    private Predicate testPredicate(String expression) {
        Stream<Predicate> stream = queryParser.predicateStream(root, expression);
        List<Predicate> list = stream.collect(Collectors.toList());
        assertEquals(1, list.size());
        return list.get(0);
    }

    @Test
    public void testCreateExpressionFromFieldStringUsesQueryFunctions() throws Exception {
        Expression<Long> expression = mock(Expression.class);
        when(em.getCriteriaBuilder().count(any())).thenReturn(expression);
        assertEquals(expression, queryParser.createExpressionFromFieldString("id.count"));
    }

    @Test
    public void testCreateExpressionFromSimpleFieldStringTriesJoiningFunction() throws Exception {
        SetJoin join = mock(SetJoin.class);
        Path expression = mock(Path.class);
        when(root.joinSet("memberProjects")).thenReturn(join);
        when(join.get("memberProjects")).thenReturn(expression);
        assertEquals(expression, queryParser.createExpressionFromFieldString("memberProjects"));
    }

    @Test
    public void testCreateExpressionFromMultiFieldStringTriesJoiningFunction() throws Exception {
        Expression<Long> expression = mock(Expression.class);
        when(em.getCriteriaBuilder().count(any())).thenReturn(expression);
        SetJoin join = mock(SetJoin.class);
        Path path = mock(Path.class);
        when(root.joinSet("memberProjects")).thenReturn(join);
        when(join.get("memberProjects")).thenReturn(path);
        assertEquals(expression, queryParser.createExpressionFromFieldString("memberProjects.count"));
        verify(em.getCriteriaBuilder()).count(path);
    }

    @Test
    public void testCreateExpressionFromSimpleFieldStringReturnsGetExpressionWhenNoJoin() throws Exception {
        Path expression = mock(Path.class);
        when(root.joinSet("memberProjects")).thenThrow(IllegalArgumentException.class);
        when(root.get("memberProjects")).thenReturn(expression);
        assertEquals(expression, queryParser.createExpressionFromFieldString("memberProjects"));
    }

    @Test
    public void testCreateExpressionFromComplexFieldStringReturnsGetExpressionWhenNoJoin() throws Exception {
        Expression<Long> expression = mock(Expression.class);
        when(em.getCriteriaBuilder().count(any())).thenReturn(expression);
        Path path = mock(Path.class);
        when(root.joinSet("memberProjects")).thenThrow(IllegalArgumentException.class);
        when(root.get("memberProjects")).thenReturn(path);
        assertEquals(expression, queryParser.createExpressionFromFieldString("memberProjects.count"));
        verify(em.getCriteriaBuilder()).count(path);
    }

    @Test
    public void testPredicateStreamToArray() throws Exception {
        List<Predicate> list = Arrays.asList(mock(Predicate.class), mock(Predicate.class));
        Predicate[] result = queryParser.predicateStreamToArray(list.stream());
        assertEquals("result should contain two predicates", 2, result.length);
        assertEquals("first element in the array should be the first element from the stream", list.get(0), result[0]);
        assertEquals("second element in the array should be the first element from the stream", list.get(1), result[1]);
    }

    @Test
    public void testErrorMessageAsJsonCreatesCorrectJsonMessage() throws Exception {
        String message = queryParser.errorMessageAsJson("error message");
        assertEquals("result should be correct json", "{\"message\": \"error message\"}", message);
    }
}