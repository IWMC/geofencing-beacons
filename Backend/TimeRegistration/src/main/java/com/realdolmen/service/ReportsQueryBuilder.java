package com.realdolmen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Typically used by endpoints and controllers to generate reports based on a dynamic query language.
 */
@Stateless
public class ReportsQueryBuilder {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private ReportsQueryParser parser = new ReportsQueryParser();

    private CriteriaQuery query;
    private Root<Employee> root;
    private Class<?> entityClazz;

    private String where;
    private String select;
    private String groupBy;
    private String orderBy;

    private List<String> fieldList;

    public ReportsQueryBuilder with(Class<?> entityClazz) {
        this.entityClazz = entityClazz;
        query = em.getCriteriaBuilder().createQuery();
        root = query.from(entityClazz);
        return this;
    }

    public ReportsQueryBuilder where(String selection) {
        this.where = selection;
        if (selection != null && !selection.isEmpty())
            query.where(parser.predicateStreamToArray(parser.createPredicatesByQuery(root, query, selection)));
        return this;
    }

    public ReportsQueryBuilder select(String projection) {
        this.select = projection;
        fieldList = projection == null ? new ArrayList<>() :
                parser.listFromRegexGroups(projection, parser.FIELD_REGEXP);

        if (fieldList.isEmpty()) {
            query.select(root);
        } else {
            query.multiselect(fieldList.stream().map(s -> parser.createExpressionFromFieldString(root, s))
                    .collect(Collectors.toList()));
        }

        return this;
    }

    public ReportsQueryBuilder groupBy(String groups) {
        this.groupBy = groups;
        if (groups != null && !groups.isEmpty()) {
            try {
                query.groupBy(parser.streamFromRegexGroups(groups, "[a-zA-Z]+").map(root::get).collect(Collectors.toList()));
            } catch (IllegalArgumentException ex) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(parser.errorMessageAsJson("Invalid group column names")).build());
            }
        }

        return this;
    }

    public ReportsQueryBuilder orderBy(String ordering) {
        return this;
    }

    public Query buildQuery(Integer firstResult, Integer maxResults) {
        Query query = em.createQuery(this.query).setFirstResult(firstResult == null ? 0 : firstResult);

        if (maxResults != null) {
            query.setMaxResults(maxResults);
        }

        return query;
    }

    /**
     * Will create an entity that can be returned as an entity in a JAX-RS request. Based on the selection it will
     * either return an entity instance or an {@link ObjectNode}, this is to improve performance.
     *
     * @return an instance of {@link ReportsQueryBuilder#entityClazz} or an {@link ObjectNode}.
     */
    public Object buildSingleResult() {
        Object result = build();
        if (result instanceof List && !((List) result).isEmpty()) {
            return ((List) result).get(0);
        } else if (result instanceof ArrayNode && ((ArrayNode) result).size() > 0) {
            return ((ArrayNode) result).get(0);
        } else {
            return null;
        }
    }

    /**
     * Will create an entity that can be returned as an entity in a JAX-RS request. Based on the selection it will
     * either return entity instances or an {@link ArrayNode}, this is to improve performance.
     *
     * @return either a list of instances of {@link ReportsQueryBuilder#entityClazz} or an {@link ArrayNode}.
     */
    public Object build() {
        return build(null, null);
    }

    /**
     * Will create an entity that can be returned as an entity in a JAX-RS request. Based on the selection it will
     * either return entity instances or an {@link ArrayNode}, this is to improve performance.
     *
     * @param firstResult the index of the first row that should be returned from the result set
     * @param maxResult   the maximum amount of rows that should be returned from the result set
     * @return either an list of instances of {@link ReportsQueryBuilder#entityClazz} or an {@link ArrayNode}.
     */
    @SuppressWarnings("unchecked")
    @Transactional
    public Object build(Integer firstResult, Integer maxResult) {
        Query query = buildQuery(firstResult, maxResult);
        if (select == null || select.isEmpty()) {
            try {
                Method method = entityClazz.getMethod("initialize", entityClazz);
                query.getResultList().stream().filter(e -> entityClazz.isAssignableFrom(e.getClass()))
                        .forEach(e -> {
                            try {
                                method.invoke(null, e);
                            } catch (InvocationTargetException | IllegalAccessException ex) {
                            }
                        });
            } catch (NoSuchMethodException e) {
            }

            return query.getResultList();
        } else {
            final JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
            ArrayNode arrayNode = nodeFactory.arrayNode();

            query.getResultList().stream().map(e -> {
                Object[] arr = e instanceof Object[] ? (Object[]) e : new Object[]{e};
                ObjectNode objectNode = nodeFactory.objectNode();
                IntStream.range(0, arr.length).forEach(i ->
                        objectNode.set(fieldList.get(i), nodeFactory.pojoNode(arr[i])));
                return objectNode;
            }).forEach(o -> arrayNode.add((JsonNode) o));

            return arrayNode;
        }
    }

    /**
     * This class is responsible for parsing the query parameters used in the RESTful APIs (under <code>com.realdolmen.rest</code>).
     * It allows the usage of query parameters in the URL to retrieve tailored data, using:
     * <ul>
     * <li>projections: which columns should be shown in the result set</li>
     * <li>selections: which records should be kept in the result set</li>
     * <li>grouping: how should the system group records for aggregate functions</li>
     * </ul>
     */
    public class ReportsQueryParser {

        public static final String FIELD_REGEXP = "[a-zA-Z]*\\.?[a-zA-Z]+";
        public static final String FIELD_PROPERTY_REGEXP = "[a-zA-Z]*\\.[a-zA-Z]+";

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

        private List<String> listFromFieldRegexGroups(String text, String regexp) {
            return streamFromFieldRegexGroups(text, regexp).collect(Collectors.toList());
        }

        public List<String> listFromRegexGroups(String text, String regexp) {
            return streamFromRegexGroups(text, regexp).collect(Collectors.toList());
        }

        public Stream<String> streamFromRegexGroups(String text, String regexp) {
            Stream.Builder<String> stream = Stream.builder();
            Matcher matcher = Pattern.compile(regexp).matcher(text);
            while (matcher.find()) {
                stream.add(matcher.group());
            }

            return stream.build();
        }

        private Stream<String> streamFromFieldRegexGroups(String text, String regexp) {
            return streamFromRegexGroups(text, FIELD_REGEXP + regexp + "\\w+");
        }

        private <E> Stream<Predicate> createPredicatesByQueryOperator(Root<E> root, CriteriaQuery query, String selection,
                                                                      String operator,
                                                                      Function<Pair<String, String>, Predicate> criteriaMap) {
            return streamFromFieldRegexGroups(selection, operator).map(kv -> {
                String[] split = kv.split(operator);
                return new Pair<>(split[0].trim(), split[1].trim());
            }).map(criteriaMap::apply).filter(p -> p != null);
        }

        @SuppressWarnings("unchecked")
        public <E> Stream<Predicate> createPredicatesByQuery(Root<E> root, CriteriaQuery query, String selection) {
            if (selection.contains("|")) {
                int operatorIndex = selection.indexOf("|");
                String first = selection.substring(0, operatorIndex);
                String second = selection.substring(operatorIndex + 1, selection.length());
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
                    return em.getCriteriaBuilder().equal(createExpressionFromFieldString(root, pair.getKey()), pair.getValue());
                } catch (IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(root, query, selection, "!=", pair -> {
                try {
                    if (!Collection.class.isAssignableFrom(root.get(pair.getKey()).type().getJavaType())) {
                        return em.getCriteriaBuilder().notEqual(createExpressionFromFieldString(root, pair.getKey()), pair.getValue());
                    } else {
                        return null;
                    }
                } catch (IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(root, query, selection, "<", pair -> {
                try {
                    return em.getCriteriaBuilder()
                            .lt((Expression<Number>) createExpressionFromFieldString(root, pair.getKey()),
                                    Double.parseDouble(pair.getValue()));
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(root, query, selection, "<=", pair -> {
                try {
                    return em.getCriteriaBuilder()
                            .lessThanOrEqualTo((Expression<Double>) createExpressionFromFieldString(root, pair.getKey()),
                                    Double.parseDouble(pair.getValue()));
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(root, query, selection, ">", pair -> {
                try {
                    return em.getCriteriaBuilder()
                            .gt((Expression<Number>) createExpressionFromFieldString(root, pair.getKey()),
                                    Double.parseDouble(pair.getValue()));
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(root, query, selection, ">=", pair -> {
                try {
                    return em.getCriteriaBuilder()
                            .greaterThanOrEqualTo((Expression<Double>) createExpressionFromFieldString(root, pair.getKey()),
                                    Double.parseDouble(pair.getValue()));
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            return streamBuilder.build();
        }

        public Expression createExpressionFromFieldString(Root root, String expression) {
            if (expression.matches(FIELD_PROPERTY_REGEXP)) {
                List<String> sides = listFromRegexGroups(expression, "[a-zA-Z]+");
                if (sides.size() == 2) {
                    try {
                        return createPropertyExpression(root, root.get(sides.get(0)), sides.get(1));
                    } catch (IllegalArgumentException iaex) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMessageAsJson(String.format("Unkown column '%s'", sides.get(0)))).build());
                    }
                } else {
                    String firstPart = expression.substring(0, expression.lastIndexOf("."));
                    return createPropertyExpression(root, createExpressionFromFieldString(root, firstPart),
                            sides.get(sides.size() - 1));
                }
            }

            try {
                try {
                    SetJoin join = root.joinSet(expression);
                    return join.get(expression);
                } catch (IllegalArgumentException iaex) {
                    return root.get(expression);
                }
            } catch (IllegalArgumentException iaex) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMessageAsJson(String.format("Unkown column '%s'", expression))).build());
            }
        }

        private Expression createPropertyExpression(Root root, Expression expression, String propertyName) {
            if (propertyName.equals("size")) {
                return em.getCriteriaBuilder().size(expression);
            } else if (propertyName.equals("avg")) {
                return em.getCriteriaBuilder().avg(expression);
            } else if (propertyName.equals("sum")) {
                return em.getCriteriaBuilder().sum(expression);
            } else if (propertyName.equals("max")) {
                return em.getCriteriaBuilder().max(expression);
            } else if (propertyName.equals("min")) {
                return em.getCriteriaBuilder().min(expression);
            } else if (propertyName.equals("count")) {
                return em.getCriteriaBuilder().count(expression);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMessageAsJson(String.format("Unkown property '%s' at column '%s'",
                                expression.toString(), propertyName)))
                        .build());
            }
        }

        public Predicate[] predicateStreamToArray(Stream<Predicate> predicates) {
            List<Predicate> predicateList = predicates.collect(Collectors.toList());
            return predicateList.toArray(new Predicate[predicateList.size()]);
        }

        public String errorMessageAsJson(String message) {
            return "{\"message\": \"" + message + "\"}";
        }

        private class Pair<K, V> {

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
}