package com.realdolmen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.realdolmen.MiscProperties;
import com.realdolmen.TestMode;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Initializable;
import com.realdolmen.entity.PersistenceUnit;
import org.jetbrains.annotations.TestOnly;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.ejb.Stateless;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;
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
@Default
public class ReportsQueryBuilder {

    public static final String SELECT_ALL = null;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private MiscProperties properties;

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
            query.where(parser.predicateStreamToArray(parser.predicateStream(root, selection)));
        return this;
    }

    public ReportsQueryBuilder select(String projection) {
        projection = projection != null ? projection.replaceAll("(salt|hash)", "") : null;
        this.select = projection;
        fieldList = projection == null ? new ArrayList<>() :
                parser.tokenList(projection, parser.FIELD_REGEXP);

        if (fieldList.isEmpty()) {
            query.select(root);
        } else {
            query.multiselect(fieldList.stream().map(parser::createExpressionFromFieldString)
                    .collect(Collectors.toList()));
        }

        return this;
    }

    public ReportsQueryBuilder groupBy(String groups) {
        this.groupBy = groups;
        if (groups != null && !groups.isEmpty()) {
            try {
                query.groupBy(parser.tokenStream(groups, "[a-zA-Z]+").map(root::get).collect(Collectors.toList()));
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

    /**
     * @return The amount of rows the query would return
     */
    public long getSize() {
        final String select = this.select;
        Query query = em.createQuery(this.query.multiselect(em.getCriteriaBuilder().count(root)));
        this.select(select);
        return (long) query.getSingleResult();
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
            query.getResultList().stream().filter(e -> e instanceof Initializable).forEach(e -> ((Initializable) e).initialize());
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

    public Root<Employee> getRoot() {
        return root;
    }

    @TestOnly
    public void setRoot(Root<Employee> root) {
        this.root = root;
    }

    public CriteriaQuery getQuery() {
        return query;
    }

    @TestOnly
    public void setQuery(CriteriaQuery query) {
        this.query = query;
    }

    protected ReportsQueryParser getParser() {
        return parser;
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
    protected class ReportsQueryParser {

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
            return tokenStreamFromFields(text, regexp).collect(Collectors.toList());
        }

        /**
         * Create a list consisting of every token in the text matching the regular expression.
         *
         * @param text   the text on which regex matching will be performed
         * @param regexp the regex that will be used to find tokens
         * @return a list of tokens
         */
        public List<String> tokenList(String text, String regexp) {
            return tokenStream(text, regexp).collect(Collectors.toList());
        }

        /**
         * Create a stream consisting of every token in the text matching the regular expression.
         *
         * @param text   the text on which regex matching will be performed
         * @param regexp the regex that will be used to find tokens
         * @return a stream of tokens
         */
        public Stream<String> tokenStream(String text, String regexp) {
            if (text == null || regexp == null) {
                return Stream.empty();
            }

            Stream.Builder<String> stream = Stream.builder();
            Matcher matcher = Pattern.compile(regexp).matcher(text);
            while (matcher.find()) {
                stream.add(matcher.group());
            }

            return stream.build();
        }

        /**
         * Create a stream of the pattern: <code>property operator value</code>. The property is an alphabetic name,
         * possibly with one dot in between two letters, e.g. <code>id</code> or <code>memberProjects.size</code>.
         * The value is any alphanumeric token, e.g. <code>Project</code> or <code>5</code>.
         * The operator is expressed as a regular expression.
         *
         * @param text   the text used to match the regex
         * @param regexp the regex for the operator that should be used
         * @return a stream of tokens matching the pattern with the specified operator
         */
        private Stream<String> tokenStreamFromFields(String text, String regexp) {
            return tokenStream(text, FIELD_REGEXP + " ?" + regexp + " ?(\\\"[\\w -]+\\\"|[\\w-]+)").map(t -> t.replace("\"", ""));
        }

        /**
         * Create a stream of predicates based on an operator and a where-clause.
         *
         * @param selection   the where-clause that will be parsed. This should be a list of
         *                    <code>field operator value</code> delimited by a non-alphanumeric value
         * @param operator    the operator that will be matched
         * @param criteriaMap a way of converting the left-hand side and right-hand side, relatively to the operator, to a predicate
         * @return a stream of predicates for the specified operator and selection
         */
        private Stream<Predicate> createPredicatesByQueryOperator(String selection,
                                                                  String operator,
                                                                  Function<Pair<String, String>, Predicate> criteriaMap) {
            return tokenStreamFromFields(selection, operator).map(kv -> {
                String[] split = kv.split(operator);
                return new Pair<>(split[0].trim(), split[1].trim());
            }).filter(p -> !p.getKey().equals("salt") && !p.getKey().equals("hash"))
                    .map(criteriaMap::apply).filter(p -> p != null);
        }

        /**
         * Create a stream of predicates for every supported operator.
         *
         * @param root      the type containing the properties
         * @param selection the where-clause
         * @return a stream of predicates
         */
        public Stream<Predicate> predicateStream(Root root, String selection) {
            if (selection == null) {
                return Stream.empty();
            }

            if (selection.contains("|")) {
                int operatorIndex = selection.indexOf("|");
                String first = selection.substring(0, operatorIndex);
                String second = selection.substring(operatorIndex + 1, selection.length());
                Predicate[] predicatesLeft = predicateStreamToArray(predicateStream(root, first));
                Predicate[] predicatesRight = predicateStreamToArray(predicateStream(root, second));
                final Predicate[] alwaysFalse = new Predicate[]{em.getCriteriaBuilder().disjunction()};

                return Stream.of(em.getCriteriaBuilder().or(
                        em.getCriteriaBuilder().and(predicatesLeft.length == 0 ? alwaysFalse : predicatesLeft),
                        em.getCriteriaBuilder().and(predicatesRight.length == 0 ? alwaysFalse : predicatesRight)
                ));
            }

            Stream.Builder<Predicate> streamBuilder = Stream.builder();
            createPredicatesByQueryOperator(selection, "=", pair -> {
                try {
                    Expression expression = createExpressionFromFieldString(pair.getKey());
                    if (Date.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().equal(expression, tryParse(pair.getValue()));
                    } else if (String.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().like(expression, "%" + pair.getValue() + "%");
                    } else if (expression.getJavaType().getSimpleName().equalsIgnoreCase("boolean")) {
                        return em.getCriteriaBuilder().equal(expression, Boolean.parseBoolean(pair.getValue()));
                    } else {
                        return em.getCriteriaBuilder().equal(expression, pair.getValue());
                    }
                } catch (IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(p -> streamBuilder.add((Predicate) p));

            createPredicatesByQueryOperator(selection, "!=", pair -> {
                try {
                    if (TestMode.isTestMode() || !Collection.class.isAssignableFrom(root.get(pair.getKey()).type().getJavaType())) {
                        Expression expression = createExpressionFromFieldString(pair.getKey());
                        if (Date.class.isAssignableFrom(expression.getJavaType())) {
                            return em.getCriteriaBuilder().notEqual(expression, tryParse(pair.getValue()));
                        } else {
                            return em.getCriteriaBuilder().notEqual(createExpressionFromFieldString(pair.getKey()), pair.getValue());
                        }
                    } else {
                        return null;
                    }
                } catch (IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(selection, "<", pair -> {
                try {
                    Expression expression = createExpressionFromFieldString(pair.getKey());
                    if (Date.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().lessThan(expression, tryParse(pair.getValue()));
                    } else {
                        return em.getCriteriaBuilder().lt((Expression<Number>) createExpressionFromFieldString(pair.getKey()),
                                Double.parseDouble(pair.getValue()));
                    }
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(selection, "<=", pair -> {
                try {
                    Expression expression = createExpressionFromFieldString(pair.getKey());
                    if (Date.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().lessThanOrEqualTo(expression, tryParse(pair.getValue()));
                    } else {
                        return em.getCriteriaBuilder()
                                .lessThanOrEqualTo((Expression<Double>) createExpressionFromFieldString(pair.getKey()),
                                        Double.parseDouble(pair.getValue()));
                    }
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(selection, ">", pair -> {
                try {
                    Expression expression = createExpressionFromFieldString(pair.getKey());
                    if (Date.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().greaterThan(expression, tryParse(pair.getValue()));
                    } else {
                        return em.getCriteriaBuilder()
                                .gt((Expression<Number>) createExpressionFromFieldString(pair.getKey()),
                                        Double.parseDouble(pair.getValue()));
                    }
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            createPredicatesByQueryOperator(selection, ">=", pair -> {
                try {
                    Expression expression = createExpressionFromFieldString(pair.getKey());
                    if (Date.class.isAssignableFrom(expression.getJavaType())) {
                        return em.getCriteriaBuilder().greaterThanOrEqualTo(expression, tryParse(pair.getValue()));
                    } else {
                        return em.getCriteriaBuilder()
                                .greaterThanOrEqualTo((Expression<Double>) createExpressionFromFieldString(pair.getKey()),
                                        Double.parseDouble(pair.getValue()));
                    }
                } catch (ClassCastException | IllegalArgumentException iaex) {
                    return null;
                }
            }).forEach(streamBuilder::add);

            return streamBuilder.build();
        }

        /**
         * Creates an expression based on a field string given by the user, e.g. <code>id.count</code> or <code>firstName</code>.
         *
         * @param expression the field string
         * @return an expression, based on the field string, retrieved from the root
         */
        public Expression createExpressionFromFieldString(String expression) {
            if (expression.matches(FIELD_PROPERTY_REGEXP)) {
                List<String> sides = tokenList(expression, "[a-zA-Z]+");
                if (sides.size() == 2) {
                    try {
                        Path<?> path = root.get(sides.get(0));
                        if (Collection.class.isAssignableFrom(path.getJavaType())) {
                            return createQueryFunctions(path, sides.get(1));
                        } else {
                            try {
                                SetJoin join = root.joinSet(sides.get(0));
                                return createQueryFunctions(join.get(sides.get(0)), sides.get(1));
                            } catch (IllegalArgumentException iaex) {
                                return createQueryFunctions(path, sides.get(1));
                            }
                        }
                    } catch (IllegalArgumentException iaex) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity(errorMessageAsJson(String.format("Unkown column '%s'", sides.get(0)))).build());
                    }
                } else {
                    String firstPart = expression.substring(0, expression.lastIndexOf("."));
                    return createQueryFunctions(createExpressionFromFieldString(firstPart),
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

        /**
         * Try converting the expression to an expression with a function using the given expression,
         * e.g. an expression for the size of a collection or the count of a group.
         *
         * @param expression   the expression on which the functions should work
         * @param functionName the name of the function, e.g. <code>size</code>, <code>avg</code>, <code>count</code>, etc.
         * @return the new expression using a function, throws a {@link WebApplicationException} otherwise
         */
        private Expression createQueryFunctions(Expression expression, String functionName) {
            if (functionName.equals("size")) {
                return em.getCriteriaBuilder().size(expression);
            } else if (functionName.equals("avg")) {
                return em.getCriteriaBuilder().avg(expression);
            } else if (functionName.equals("sum")) {
                return em.getCriteriaBuilder().sum(expression);
            } else if (functionName.equals("max")) {
                return em.getCriteriaBuilder().max(expression);
            } else if (functionName.equals("min")) {
                return em.getCriteriaBuilder().min(expression);
            } else if (functionName.equals("count")) {
                return em.getCriteriaBuilder().count(expression);
            } else if (expression instanceof Path) {
                return ((Path) expression).get(functionName);
            } else {
                return expression;
            }
        }

        /**
         * Converts a stream of predicates to an array of predicates.
         *
         * @param predicates the stream of predicates
         * @return the array of predicates
         */
        public Predicate[] predicateStreamToArray(Stream<Predicate> predicates) {
            List<Predicate> predicateList = predicates.collect(Collectors.toList());
            return predicateList.toArray(new Predicate[predicateList.size()]);
        }

        /**
         * Creates a JSON error message based on a simple message.
         *
         * @param message the content of the message
         * @return the message in JSON form
         */
        public String errorMessageAsJson(String message) {
            return "{\"message\": \"" + message + "\"}";
        }

        private Date tryParse(String dateString) {
            for (String pattern : properties.getString("DateFormats").split(";")) {
                try {
                    DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern(pattern).withLocale(Locale.forLanguageTag("nl")));
                    if (dateTime != null) {
                        return dateTime.toDate();
                    }
                } catch (IllegalArgumentException iaex) {
                }

                try {
                    DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern(pattern).withLocale(Locale.FRENCH));
                    if (dateTime != null) {
                        return dateTime.toDate();
                    }
                } catch (IllegalArgumentException iaex) {
                }

                try {
                    DateTime dateTime = DateTime.parse(dateString, DateTimeFormat.forPattern(pattern));
                    if (dateTime != null) {
                        return dateTime.toDate();
                    }
                } catch (IllegalArgumentException iaex) {
                }
            }

            return null;
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