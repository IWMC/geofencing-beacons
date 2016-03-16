package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.*;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.json.EmployeePasswordCredentials;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.hibernate.Hibernate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.realdolmen.annotations.UserGroup.*;


/**
 * Endpoint for managing employees. However management <b>related</b> to employees, such as project manager assignment, is not
 * enclosed in this endpoint.
 */
@Stateless
@Path("/employees")
public class EmployeeEndpoint {

	@PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
	private EntityManager em;

    @Inject
    private Validator<EmployeePasswordCredentials> credentialsValidator;

    @Inject
    private Validator<Employee> employeeValidator;

    @Inject
    private SecurityManager securityManager;

	@DELETE
	@Path("/{id:[0-9]+}")
    @Authorized(MANAGEMENT_EMPLOYEE_ONLY)
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
    @Authorized(MANAGEMENT)
	public Response findById(@PathParam("id") Long id) {
		Employee entity = em.find(Employee.class, id);

		if (entity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

        Hibernate.initialize(entity.getMemberProjects());
		return Response.ok(entity).build();
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(MANAGEMENT)
	public Response listAll(@QueryParam("start") Integer startPosition,
			@QueryParam("max") Integer maxResult) {
		TypedQuery<Employee> findAllQuery = em.createNamedQuery("Employee.findAll", Employee.class);

		if (startPosition != null) {
			findAllQuery.setFirstResult(startPosition);
		}

        if (maxResult != null) {
			findAllQuery.setMaxResults(maxResult);
		}

        List<Employee> employees = findAllQuery.getResultList();
        employees.forEach(Employee::initialize);
		return Response.ok().entity(employees).build();
	}

    @PUT
    @Path("/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(EMPLOYEE)
    @Transactional
    public Response updatePassword(@PathParam("id") Long id, EmployeePasswordCredentials credentials) throws NoSuchAlgorithmException {
        ValidationResult validationResult = credentialsValidator.validate(credentials);
        if (!validationResult.isValid()) {
            return Response.status(Status.BAD_REQUEST).entity(validationResult).build();
        }

        Employee dbEmployee = em.find(Employee.class, id);
        dbEmployee.setPassword(credentials.getPassword());
        dbEmployee.setSalt(securityManager.randomSalt());
        dbEmployee.setHash(securityManager.generateHash(dbEmployee.getSalt(), dbEmployee.getPassword()));
        em.merge(dbEmployee);

        return Response.noContent().build();
    }

	@PUT
	@Path("/{id:[0-9]+}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(MANAGEMENT_EMPLOYEE_ONLY)
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

        Employee dbEmployee = em.find(Employee.class, id);
		if (dbEmployee == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

        ValidationResult validationResult = employeeValidator.validate(entity, Existing.class);
        if (validationResult != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationResult).build();
        }

        entity.setSalt(dbEmployee.getSalt());
        entity.setHash(dbEmployee.getHash());

		try {
            em.merge(entity);
		} catch (OptimisticLockException e) {
			return Response.status(Response.Status.CONFLICT)
					.entity(e.getEntity()).build();
		} catch (NonUniqueResultException nure) {
            return Response.status(Response.Status.CONFLICT).build();
        }

		return Response.noContent().build();
	}

    @PUT
    @Path("/{id:[0-9]+}/upgrade/management-employee")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(MANAGEMENT_EMPLOYEE_ONLY)
    public Response upgradeManagementEmployee(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Employee employee = em.find(Employee.class, id);

        if (employee == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        ManagementEmployee newManager = new ManagementEmployee(employee);
        employee.getMemberProjects().stream().map(Project::getEmployees).forEach(e -> {
            e.remove(employee);
            e.add(newManager);
        });
        employee.getRegisteredOccupations().stream().forEach(o -> o.setRegistrar(newManager));
        em.remove(employee);

        try {
            em.persist(newManager);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("/{id:[0-9]+}/upgrade/project-manager")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(MANAGEMENT_EMPLOYEE_ONLY)
    public Response upgradeProjectManager(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Employee employee = em.find(Employee.class, id);

        if (employee == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        ProjectManager newManager = new ProjectManager(employee);
        employee.getMemberProjects().stream().map(Project::getEmployees).forEach(e -> {
            e.remove(employee);
            e.add(newManager);
        });
        employee.getRegisteredOccupations().stream().forEach(o -> o.setRegistrar(newManager));
        em.remove(employee);

        try {
            em.persist(newManager);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("/{id:[0-9]+}/upgrade/project-manager")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorized(MANAGEMENT_EMPLOYEE_ONLY)
    public Response downgradeEmployee(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Employee employee = em.find(Employee.class, id);

        if (employee == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        Employee newEmployee = new Employee(employee);
        employee.getMemberProjects().stream().map(Project::getEmployees).forEach(e -> {
            e.remove(employee);
            e.add(newEmployee);
        });
        employee.getRegisteredOccupations().stream().forEach(o -> o.setRegistrar(newEmployee));
        em.remove(employee);

        try {
            em.persist(newEmployee);
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getEntity()).build();
        }

        return Response.noContent().build();
    }
}
