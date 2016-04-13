package com.realdolmen.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.service.SecurityManager;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by FDMAZ46 on 7/04/2016.
 */
@Path("reports")
@Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
@Stateless
public class ReportsEndpoint {

    public static final String PROJECTION_DELIMITER = ",";

    @Inject
    private EmployeeEndpoint employeeEndpoint;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @Inject
    private SecurityManager securityManager;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    // region Employees

    public List<Employee> listEmployeesInternal(Integer startPosition, Integer max) {
        return (List<Employee>) employeeEndpoint.listAll(startPosition, max).getEntity();
    }

    @GET
    @Path("employees")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @SuppressWarnings("unchecked")
    public Response filteredEmployeeList(@QueryParam("values") String projection,
                                         @QueryParam("where") String selection,
                                         @QueryParam("start") Integer startPosition,
                                         @QueryParam("max") Integer max) {
        CriteriaQuery query = em.getCriteriaBuilder().createQuery();
        Root<Employee> root = query.from(Employee.class);
        List<String> fieldList = streamFromRegexGroups(projection, "[a-zA-Z]").collect(Collectors.toList());

        if (selection != null)
            query.where(predicateStreamToArray(createPredicatesByQuery(root, query, selection)));

        if (projection == null || projection.isEmpty()) {
            query.select(root);
        } else {
            query.multiselect(fieldList.stream().map(root::get).collect(Collectors.toList()));
        }

        Query employeeQuery = em.createQuery(query);
        if (projection == null || projection.isEmpty()) {
            employeeQuery.getResultList().stream().filter(e -> e instanceof Employee)
                    .forEach(e -> Employee.initialize((Employee) e));
            return Response.ok(employeeQuery.getResultList()).build();
        } else {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            employeeQuery.getResultList().stream().map(e -> {
                Object[] arr = (Object[]) e;
                JsonObjectBuilder builder = Json.createObjectBuilder();
                IntStream.range(0, arr.length).forEach(i -> builder.add(fieldList.get(i), arr[i].toString()));
                return builder.build();
            }).forEach(o -> arrayBuilder.add((JsonObject) o));

            return Response.ok(arrayBuilder.build()).build();
        }
    }

    // endregion

    // region Single employee

    @Authorized
    public Employee employeeDetailsInternal() {
        Employee employee = securityManager.findEmployee();
        Employee.initialize(employee);
        return employee;
    }

    @GET
    @Path("employees/me")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized
    public Response employeeDetails(@QueryParam("values") String projection) {
        if (projection == null || projection.isEmpty()) {
            return Response.ok(employeeDetailsInternal()).build();
        }

        Employee employee = employeeDetailsInternal();
        JsonNode node = mapToJsonNode(employee);
        filterNodeBySelectionArgs(node, new ArrayList<>(Arrays.asList(projection.split(PROJECTION_DELIMITER))));
        return Response.ok(node).build();
    }

    // endregion

    // region Occupations

    /*@GET
    @Path("occupations/all")
    public List<Occupation> listOccupations(@QueryParam("start") Integer startPosition,
                                            @QueryParam("max") Integer max) {

    }*/

    // endregion

    private JsonNode mapToJsonNode(Object obj) {
        return new ObjectMapper().valueToTree(obj);
    }

    private void filterNodeBySelectionArgs(JsonNode node, List<String> selectionArgs) {
        if (node.isArray()) {
            node.forEach(e -> filterNodeBySelectionArgs(e, selectionArgs));
        }

        Stream.Builder<String> streamBuilder = Stream.builder();
        node.fieldNames().forEachRemaining(streamBuilder::add);
        streamBuilder.build().filter(e -> !selectionArgs.contains(e)).forEach(key -> {
            if (node.isObject()) {
                ((ObjectNode) node).remove(key);
            }
        });
    }

    private Stream<String> streamFromRegexGroups(String text, String regexp) {
        Stream.Builder<String> stream = Stream.builder();
        Matcher matcher = Pattern.compile("[a-zA-Z]+" + regexp + "\\w+").matcher(text);
        while (matcher.find()) {
            stream.add(matcher.group());
        }

        return stream.build();
    }

    private <E> Stream<Predicate> createPredicatesByQueryOperator(Root<E> root, CriteriaQuery query, String selection,
                                                                  String operator,
                                                                  Function<Pair<String, String>, Predicate> criteriaMap) {
        return streamFromRegexGroups(selection, operator).map(kv -> {
            String[] split = kv.split(operator);
            return new Pair<>(split[0].trim(), split[1].trim());
        }).map(criteriaMap::apply).filter(p -> p != null);
    }

    private <E> Stream<Predicate> createPredicatesByQuery(Root<E> root, CriteriaQuery query, String selection) {
        if (selection.contains("|")) {
            int operatorIndex = selection.indexOf("|");
            String first = selection.substring(0, selection.indexOf("|"));
            String second = selection.substring(selection.indexOf("|") + 1, selection.length());
            Predicate[] predicatesLeft = predicateStreamToArray(createPredicatesByQuery(root, query, first));
            Predicate[] predicatesRight = predicateStreamToArray(createPredicatesByQuery(root, query, second));
            final Predicate[] alwaysFalse = new Predicate[]{em.getCriteriaBuilder().disjunction()};

            return Stream.of(em.getCriteriaBuilder().or(
                    em.getCriteriaBuilder().and(predicatesLeft.length == 0 ? alwaysFalse : predicatesLeft),
                    em.getCriteriaBuilder().and(predicatesRight.length == 0 ? alwaysFalse : predicatesRight)
            ));
        }

        Stream.Builder<Predicate> streamBuilder = Stream.builder();
        createPredicatesByQueryOperator(root, query, selection, "=", pair -> {
            try {
                return em.getCriteriaBuilder().equal(root.get(pair.getKey()), pair.getValue());
            } catch (IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        createPredicatesByQueryOperator(root, query, selection, "!=", pair -> {
            try {
                if (!Collection.class.isAssignableFrom(root.get(pair.getKey()).type().getJavaType())) {
                    return em.getCriteriaBuilder().notEqual(root.get(pair.getKey()), pair.getValue());
                } else {
                    return null;
                }
            } catch (IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        createPredicatesByQueryOperator(root, query, selection, "<", pair -> {
            try {
                return em.getCriteriaBuilder().lt(root.get(pair.getKey()), Double.parseDouble(pair.getValue()));
            } catch (ClassCastException | IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        createPredicatesByQueryOperator(root, query, selection, "<=", pair -> {
            try {
                return em.getCriteriaBuilder().lessThanOrEqualTo(root.get(pair.getKey()), Double.parseDouble(pair.getValue()));
            } catch (ClassCastException | IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        createPredicatesByQueryOperator(root, query, selection, ">", pair -> {
            try {
                return em.getCriteriaBuilder().gt(root.get(pair.getKey()), Double.parseDouble(pair.getValue()));
            } catch (ClassCastException | IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        createPredicatesByQueryOperator(root, query, selection, ">=", pair -> {
            try {
                return em.getCriteriaBuilder().greaterThanOrEqualTo(root.get(pair.getKey()), Double.parseDouble(pair.getValue()));
            } catch (ClassCastException | IllegalArgumentException iaex) {
                return null;
            }
        }).forEach(streamBuilder::add);

        return streamBuilder.build();
    }

    private Predicate[] predicateStreamToArray(Stream<Predicate> predicates) {
        List<Predicate> predicateList = predicates.collect(Collectors.toList());
        return predicateList.toArray(new Predicate[predicateList.size()]);
    }

    private static class Pair<K, V> {

        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
