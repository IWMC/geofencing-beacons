package com.realdolmen.rest;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.validation.New;
import com.realdolmen.json.JsonWebToken;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.jetbrains.annotations.TestOnly;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.security.NoSuchAlgorithmException;

/**
 * JAX-RS endpoint for registration and login. Management of employees not related to login and registration is provided
 * in {@link EmployeeEndpoint}.
 */
@Stateless
@Path("user")
public class UserEndpoint {

    @Inject
    private Validator<Employee> validator;

    @Inject
    private SecurityManager securityManager;

    @PersistenceContext(unitName = "TimeRegistration-test-persistence-unit")
    private EntityManager em;

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Employee entity) throws NoSuchAlgorithmException {
        if (entity == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Entity must not be null").build();
        }

        ValidationResult result = validator.validate(entity, New.class);

        if (!result.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        }

        String salt = securityManager.randomSalt();
        entity.setSalt(salt);
        entity.setHash(securityManager.generateHash(salt, entity.getPassword()));

        em.persist(entity);
        return Response.created(
                UriBuilder.fromResource(EmployeeEndpoint.class)
                        .path(String.valueOf(entity.getId())).build()).build();
    }

    /**
     * @param employee the credentials with which the user tries to log in
     * @return 200 code with a JWT_KEY used as a session token for later validation or 400 code
     */
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Employee employee) throws NoSuchAlgorithmException {
        if (employee.getUsername() == null || employee.getUsername().isEmpty() || employee.getPassword() == null
                || employee.getPassword().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Employee dbEmployee;

        try {
            dbEmployee = em.createNamedQuery("Employee.findByUsername", Employee.class).setParameter("username", employee.getUsername()).getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        boolean isValid = securityManager.checkPassword(dbEmployee, employee.getPassword());
        if (!isValid) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().entity(new JsonWebToken(securityManager.generateToken(dbEmployee))).build();
    }

    @TestOnly
    public void setEntityManager(EntityManager entityManager) {
        this.em = entityManager;
    }
}
