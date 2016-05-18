package com.realdolmen.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.Beacon;
import com.realdolmen.entity.Initializable;
import com.realdolmen.service.SecurityManager;

/**
 *
 */
@Stateless
@Path("/beacons")
public class BeaconEndpoint {

    @Inject
    private SecurityManager sm;

    @PersistenceContext(unitName = "TimeRegistration-persistence-unit")
    private EntityManager em;

    @POST
    @Consumes("application/json")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Transactional
    public Response create(Beacon entity) {
        em.persist(entity);
        return Response.created(
                UriBuilder.fromResource(BeaconEndpoint.class)
                        .path(String.valueOf(entity.getId())).build()).build();
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Transactional
    public Response deleteById(@PathParam("id") Long id) {
        Beacon entity = em.find(Beacon.class, id);
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        em.remove(entity);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("application/json")
    @Authorized
    public Response findById(@PathParam("id") Long id) {
        Beacon entity;
        try {
            entity = em.find(Beacon.class, id);
        } catch (NoResultException nre) {
            entity = null;
        }
        if (entity == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        entity.initialize();
        return Response.ok(entity).build();
    }

    @GET
    @Produces("application/json")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Transactional
    public Response listAll(@QueryParam("start") Integer startPosition,
                                @QueryParam("max") Integer maxResult) {
        TypedQuery<Beacon> findAllQuery = em.createNamedQuery("Beacon.findAll", Beacon.class);
        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }

        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }

        final List<Beacon> results = findAllQuery.getResultList();
        results.forEach(Initializable::initialize);
        return Response.ok(results).build();
    }

    @Path("me")
    @GET
    @Produces("application/json")
    @Authorized
    @Transactional
    public Response listAllForEmployee(@QueryParam("start") Integer startPosition,
                                           @QueryParam("max") Integer maxResult) {
        TypedQuery<Beacon> findAllQuery = em.createNamedQuery("Beacon.findAllForEmployee", Beacon.class);
        findAllQuery.setParameter("employee", sm.findEmployee());
        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }

        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }

        final List<Beacon> results = findAllQuery.getResultList();
        results.forEach(Initializable::initialize);
        return Response.ok(results).build();
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Transactional
    public Response update(@PathParam("id") Long id, Beacon entity) {
        if (entity == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        if (!id.equals(entity.getId())) {
            return Response.status(Status.CONFLICT).entity(entity).build();
        }
        if (em.find(Beacon.class, id) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        try {
            entity = em.merge(entity);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }
}
