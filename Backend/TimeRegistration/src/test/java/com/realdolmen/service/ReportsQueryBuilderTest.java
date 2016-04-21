package com.realdolmen.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Initializable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportsQueryBuilderTest {

    @Mock
    private EntityManager em;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery query;

    @Mock
    private Root root;

    @Mock
    private ReportsQueryBuilder.ReportsQueryParser parser;

    @InjectMocks
    private ReportsQueryBuilder queryBuilder = new ReportsQueryBuilder();

    @Mock
    private TypedQuery emQuery;

    @Before
    public void setUp() throws Exception {
        when(em.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery()).thenReturn(query);
        when(criteriaBuilder.createQuery(any())).thenReturn(query);
        when(query.from(any(Class.class))).thenReturn(root);
        when(parser.predicateStreamToArray(any())).thenCallRealMethod();
        when(em.createQuery(query)).thenReturn(emQuery);
        when(emQuery.setFirstResult(anyInt())).thenReturn(emQuery);
        when(emQuery.setMaxResults(anyInt())).thenReturn(emQuery);
    }

    @Test
    public void testWithCreatesQuery() throws Exception {
        queryBuilder.setQuery(null);
        queryBuilder.with(Employee.class);
        assertNotNull(queryBuilder.getQuery());
        assertEquals(query, queryBuilder.getQuery());
    }

    @Test
    public void testWithCreatesRoot() throws Exception {
        queryBuilder.setRoot(null);
        queryBuilder.with(Employee.class);
        assertNotNull(queryBuilder.getRoot());
        assertEquals(root, queryBuilder.getRoot());
        verify(query).from(Employee.class);
    }

    @Test
    public void testWhereDoesNothingOnNull() throws Exception {
        queryBuilder.where(null);
        verify(query, never()).where(Mockito.<Predicate>anyVararg());
        verify(query, never()).where(Mockito.<Expression<Boolean>>anyVararg());
    }

    @Test
    public void testWherePassesPredicatesToQuery() throws Exception {
        final String selection = "id=5";
        Predicate mock = mock(Predicate.class);
        Stream<Predicate> predicateStream = Stream.of(mock);
        when(parser.predicateStream(root, selection)).thenReturn(predicateStream);
        when(parser.predicateStreamToArray(predicateStream)).thenReturn(new Predicate[]{mock});

        queryBuilder.where(selection);
        verify(query).where(new Predicate[]{mock});
    }

    @Test
    public void testSelectSelectsSingleSelectOnEmptyProjection() throws Exception {
        when(parser.tokenList(eq(""), anyString())).thenReturn(new ArrayList<>());
        queryBuilder.select("");
        verify(query).select(root);
    }

    @Test
    public void testSelectSelectsSingleSelectOnNullProjection() throws Exception {
        queryBuilder.select(null);
        verify(query).select(root);
    }

    @Test
    public void testSelectSelectsMultiSelectOnProjection() throws Exception {
        final String selection = "firstName";
        when(parser.tokenList(selection, ReportsQueryBuilder.ReportsQueryParser.FIELD_REGEXP))
                .thenReturn(Arrays.asList(selection));
        Expression mock = mock(Expression.class);
        when(parser.createExpressionFromFieldString(selection)).thenReturn(mock);

        queryBuilder.select(selection);
        verify(query, never()).select(any());
        verify(query).multiselect(Arrays.asList(mock));
    }

    @Test
    public void testGroupByDoesNothingOnNullOrEmpty() throws Exception {
        queryBuilder.groupBy(null);
        verify(query, never()).groupBy(anyList());
        verify(query, never()).groupBy(Mockito.<Expression>anyVararg());
    }

    @Test
    public void testGroupByGroupsByInput() throws Exception {
        final String group = "id";
        Path path = mock(Path.class);
        when(root.get(group)).thenReturn(path);
        when(parser.tokenStream(group, "[a-zA-Z]+")).thenReturn(Stream.of(group));
        queryBuilder.groupBy(group);
        verify(query).groupBy(Arrays.asList(path));
    }

    @Test(expected = WebApplicationException.class)
    public void testGroupByHandlesInvalidColumnName() throws Exception {
        final String group = "id";
        mock(Path.class);
        when(root.get(group)).thenThrow(IllegalArgumentException.class);
        when(parser.tokenStream(group, "[a-zA-Z]+")).thenReturn(Stream.of(group));
        queryBuilder.groupBy(group);
    }

    @Test
    public void testBuildQuerySetsFirstResult() throws Exception {
        queryBuilder.buildQuery(10, null);
        verify(emQuery).setFirstResult(10);
        verify(emQuery, never()).setMaxResults(anyInt());
    }

    @Test
    public void testBuildQuerySetsMaxResult() throws Exception {
        queryBuilder.buildQuery(null, 10);
        verify(emQuery).setFirstResult(0);
        verify(emQuery).setMaxResults(10);
    }

    @Test
    public void testBuildQuerySetsFirstAndMaxResult() throws Exception {
        queryBuilder.buildQuery(1, 2);
        verify(emQuery).setFirstResult(1);
        verify(emQuery).setMaxResults(2);
    }

    @Test
    public void testBuildQueryReturnsCorrectQueryInstance() throws Exception {
        Query query = queryBuilder.buildQuery(null, null);
        assertEquals("should return the correct query", emQuery, query);
    }

    @Test
    public void testBuildSingleResult() throws Exception {
    }

    @Test
    public void testBuild() throws Exception {

    }

    @Test
    public void testBuildUsesFirstResultAndMaxResult() throws Exception {
        queryBuilder.build(1, 2);
        verify(emQuery).setFirstResult(1);
        verify(emQuery).setMaxResults(2);
    }

    @Test
    public void testBuildInitializesEntities() throws Exception {
        Initializable initializable = mock(Initializable.class);

        when(emQuery.getResultList()).thenReturn(Arrays.asList(initializable));
        queryBuilder.build();
        verify(initializable).initialize();
    }

    @Test
    public void testBuildReturnsResultList() throws Exception {
        List list = Arrays.asList(mock(Initializable.class));
        when(emQuery.getResultList()).thenReturn(list);
        Object result = queryBuilder.build();
        assertEquals(list, result);
    }

    @Test
    public void testBuildCreatesAnArrayNodeWithSelection() throws Exception {
        List list = Arrays.asList(new Object[]{5}, new Object[]{6});
        when(emQuery.getResultList()).thenReturn(list);
        when(parser.tokenList("id", ReportsQueryBuilder.ReportsQueryParser.FIELD_REGEXP)).thenReturn(Arrays.asList("id"));
        Object obj = queryBuilder.select("id").build();
        assertTrue("result should be an ArrayNode", obj instanceof ArrayNode);
    }

    @Test
    public void testBuildUsesSelectionFieldsInObjectNodes() throws Exception {
        List list = Arrays.asList(new Object[]{5}, new Object[]{6});
        when(emQuery.getResultList()).thenReturn(list);
        when(parser.tokenList("id", ReportsQueryBuilder.ReportsQueryParser.FIELD_REGEXP)).thenReturn(Arrays.asList("id"));
        ArrayNode node = (ArrayNode) queryBuilder.select("id").build();
        assertEquals("response should contain two object nodes", 2, node.size());
        assertTrue("node should be an object", node.get(0).isObject());
        assertTrue("node should be an object", node.get(1).isObject());
        assertEquals(5, node.get(0).get("id").asInt());
        assertEquals(6, node.get(1).get("id").asInt());
    }
}