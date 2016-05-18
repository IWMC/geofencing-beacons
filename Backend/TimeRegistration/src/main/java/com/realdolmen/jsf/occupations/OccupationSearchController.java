package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Task;
import com.realdolmen.rest.OccupationEndpoint;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A controller for <code>/occupations/search-occupations.xhtml</code>.
 */
@Named("occupationSearch")
@RequestScoped
@Default
public class OccupationSearchController {

    @Inject
    private OccupationEndpoint endpoint;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    private String searchTerms;

    @Transactional
    public List<Occupation> getOccupationsWithSearchTerms() {
        if (getSearchTerms() == null || getSearchTerms().isEmpty()) {
            return getOccupations();
        }

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);

        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Occupation.class).get();
        org.apache.lucene.search.Query luceneQuery = qb
                .bool().should(qb.keyword().onFields("description", "name").matching(searchTerms).createQuery())
                .createQuery();

        javax.persistence.Query jpaQuery =
                fullTextEntityManager.createFullTextQuery(luceneQuery, Occupation.class);

        Stream<Occupation> result = jpaQuery.getResultList().stream().filter(o -> !(o instanceof Task));
        return result.collect(Collectors.toList());
    }

    @Transactional
    public List<Occupation> getOccupations() {
        if (searchTerms == null || searchTerms.isEmpty()) {
            Response response = endpoint.listAll(null, null);
            List<Occupation> occupations = (List<Occupation>) response.getEntity();
            return occupations;
        } else {
            return getOccupationsWithSearchTerms();
        }
    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public OccupationEndpoint getOccupationEndpoint() {
        return endpoint;
    }

    public EntityManager getEntityManager() {
        return em;
    }
}
