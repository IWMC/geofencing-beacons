package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.Project;
import com.realdolmen.service.ReportsQueryBuilder;
import com.realdolmen.service.SecurityManager;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Endpoint used for creating reports of employees, occupations or projects.
 */
@Path("reports")
@Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
@Stateless
public class ReportsEndpoint {

    @Inject
    private EmployeeEndpoint employeeEndpoint;

    @Inject
    private OccupationEndpoint occupationEndpoint;

    @Inject
    private SecurityManager securityManager;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private ReportsQueryBuilder builder;

    // region Employees

    public List<Employee> listEmployeesInternal(Integer startPosition, Integer max) {
        return (List<Employee>) employeeEndpoint.listAll(startPosition, max).getEntity();
    }

    @GET
    @Path("employees")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response filteredEmployeeList(@QueryParam("values") String projection,
                                         @QueryParam("where") String selection,
                                         @QueryParam("group") String groups,
                                         @QueryParam("start") Integer startPosition,
                                         @QueryParam("max") Integer max) {
        return Response.ok(builder
                .with(Employee.class)
                .select(projection)
                .where(selection)
                .groupBy(groups)
                .build(startPosition, max)).build();
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
        return Response.ok(builder
                .with(Employee.class)
                .select(projection)
                .where("id=" + securityManager.findEmployee().getId())
                .groupBy("id")
                .buildSingleResult()).build();
    }

    // endregion

    // region Occupations

    @GET
    @Path("occupations")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response filteredOccupationList(@QueryParam("values") String projection,
                                         @QueryParam("where") String selection,
                                         @QueryParam("group") String groups,
                                         @QueryParam("start") Integer startPosition,
                                         @QueryParam("max") Integer max) {
        return Response.ok(builder
                .with(Occupation.class)
                .select(projection)
                .where(selection)
                .groupBy(groups)
                .build(startPosition, max)).build();
    }

    @GET
    @Path("projects")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response filteredProjectList(@QueryParam("values") String projection,
                                           @QueryParam("where") String selection,
                                           @QueryParam("group") String groups,
                                           @QueryParam("start") Integer startPosition,
                                           @QueryParam("max") Integer max) {
        return Response.ok(builder
                .with(Project.class)
                .select(projection)
                .where(selection)
                .groupBy(groups)
                .build(startPosition, max)).build();
    }

    // endregion


}
