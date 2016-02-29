package com.realdolmen.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.realdolmen.entity.Employee;

/**
 * Endpoint for managing employees. However management <b>related</b> to employees, such as project manager assignment, is not
 * enclosed in this endpoint.
 */
@Stateless
@Path("/employees")
public class EmployeeEndpoint {

	@PersistenceContext(unitName = "TimeRegistration-persistence-unit")
	private EntityManager em;

	@DELETE
	@Path("/{id:[0-9]+}")
	public Response deleteById(@PathParam("id") Long id) {
		Employee entity = em.find(Employee.class, id);
		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	@GET
	@Path("/{id:[0-9]+}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response findById(@PathParam("id") Long id) {
		Employee entity = em.find(Employee.class, id);

		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok(entity).build();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Employee> listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		TypedQuery<Employee> findAllQuery = em.createNamedQuery("Employee.findAll", Employee.class);

		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}

		return findAllQuery.getResultList();
	}

	@PUT
	@Path("/{id:[0-9]+}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response update(@PathParam("id") Long id, Employee entity) {
		if (entity == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (id == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (!id.equals(entity.getId())) {
			return Response.status(Status.CONFLICT).entity(entity).build();
		}
		if (em.find(Employee.class, id) == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		try {
            em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		}

		return Response.noContent().build();
	}
}
