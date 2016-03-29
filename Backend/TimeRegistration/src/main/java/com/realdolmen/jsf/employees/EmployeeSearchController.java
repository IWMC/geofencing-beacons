package com.realdolmen.jsf.employees;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.rest.EmployeeEndpoint;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.List;

/**
 * A controller for <code>/employees/search-employees.xhtml</code>.
 */
@Named("employeeSearch")
@RequestScoped
public class EmployeeSearchController implements Serializable {

    @Inject
    private EmployeeEndpoint endpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private String searchTerms;

    @Transactional
    public List<Employee> getEmployeesWithSearchTerms() {
        if (getSearchTerms() == null || getSearchTerms().isEmpty()) {
            return getEmployees();
        }

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Employee.class).get();
        org.apache.lucene.search.Query luceneQuery = qb
                .bool().should(qb.keyword().onField("username").matching(searchTerms).createQuery())
                .should(qb.phrase().onField("email").sentence(searchTerms).createQuery())
                .should(qb.keyword().onFields("firstName", "lastName").matching(searchTerms).createQuery())
                .createQuery();

        javax.persistence.Query jpaQuery =
                fullTextEntityManager.createFullTextQuery(luceneQuery, Employee.class);

        List<Employee> result = jpaQuery.getResultList();
        return result;
    }

    @Transactional
    public List<Employee> getEmployees() {
        if (searchTerms == null || searchTerms.isEmpty()) {
            Response response = endpoint.listAll(0, 0);
            List<Employee> employees = (List<Employee>) response.getEntity();
            return employees;
        } else {
            return getEmployeesWithSearchTerms();
        }
    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public EntityManager getEntityManager() {
        return em;
    }
}
