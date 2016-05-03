package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.entity.validation.New;
import com.realdolmen.json.Json;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;

import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Endpoint for removing and adding tasks and add tasks to existing projects.
 */
@Path("tasks")
public class TaskEndpoint {

    @Inject
    private Validator<Task> validator;

    @Inject
    private SecurityManager sm;

    @Context
    private UriInfo uriInfo;

    @Inject
    private TaskDao taskDao;

    @GET
    @Authorized
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Transactional
    public Response getTasks(@QueryParam("start") Integer startPosition,
                             @QueryParam("max") Integer maxResult) {
        TypedQuery<Task> query = taskDao.getTasks();
        if (startPosition != null) {
            query.setFirstResult(startPosition);
        }

        if (maxResult != null) {
            query.setMaxResults(maxResult);
        }

        return Response.ok(query.getResultList()).build();
    }

    @GET
    @Path("{projectNr}")
    @Authorized
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTasksByProject(@QueryParam("start") Integer startPosition,
                                      @QueryParam("max") Integer maxResult,
                                      @PathParam("projectNr") String projectNr) {
        if (projectNr == null || projectNr.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Json.error("projectNr cannot be empty")).build();
        }

        TypedQuery<Task> query = taskDao.getTasksByProject(projectNr);
        if (startPosition != null) {
            query.setFirstResult(startPosition);
        }

        if (maxResult != null) {
            query.setMaxResults(maxResult);
        }

        return Response.ok(query.getResultList()).build();
    }

    @POST
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addTask(Task task) {
        if (task.getProjectId() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(com.realdolmen.json.Json.error("invalid projectNr"))
                    .build();
        }

        if (!taskDao.isManagingProjectManager(task.getProjectId(), sm.findEmployee())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(com.realdolmen.json.Json.error("project with projectId " + task.getProjectId() + " is not managed by " + sm.findEmployee().getName()))
                    .build();
        }

        ValidationResult result = validator.validate(task, New.class);
        if (!result.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
        }

        try {
            taskDao.addTask(task);
        } catch (EJBTransactionRolledbackException etrex) {
            if (etrex.getCause() instanceof IllegalArgumentException) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Json.error(etrex.getCause().getMessage()))
                        .build();
            }
        }

        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(task.getId())).build()).build();
    }

    @DELETE
    @Path("/{id}")
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    @Transactional
    public Response removeTask(@PathParam("id") long projectNr) {
        taskDao.removeTask(projectNr);
        return Response.noContent().build();
    }
}