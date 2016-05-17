package com.realdolmen.rest;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.entity.validation.New;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskEndpointTest {

    @Mock
    private Validator<Task> validator;

    @Mock
    private TaskDao taskDao;

    @Mock
    private SecurityManager sm;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private TaskEndpoint endpoint = new TaskEndpoint();

    private Project project = new Project();

    private Task task = new Task("Task name", "Task description", 5d, null);

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetTasksUsesTaskDao() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasks()).thenReturn(query);
        endpoint.getTasks(0, 0);
        verify(taskDao).getTasks();
    }

    @Test
    public void testGetTasksSetsStartPositionIfNotNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasks()).thenReturn(query);
        endpoint.getTasks(10, 0);
        verify(taskDao).getTasks();
        verify(query).setFirstResult(10);
    }

    @Test
    public void testGetTasksDoesNotSetStartPositionIfNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasks()).thenReturn(query);
        endpoint.getTasks(null, 0);
        verify(taskDao).getTasks();
        verify(query, never()).setFirstResult(anyInt());
    }

    @Test
    public void testGetTasksSetsMaxResultsIfNotNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasks()).thenReturn(query);
        endpoint.getTasks(0, 10);
        verify(taskDao).getTasks();
        verify(query).setMaxResults(10);
    }

    @Test
    public void testGetTasksDoesNotSetMaxResultsIfNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasks()).thenReturn(query);
        endpoint.getTasks(0, null);
        verify(taskDao).getTasks();
        verify(query, never()).setMaxResults(anyInt());
    }

    @Test
    public void testGetTasksByProjectReturnsBadRequestIfNoProjectNr() throws Exception {
        Response result = endpoint.getTasksByProject(0, 0, "");
        assertEquals("response should be bad request", Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        result = endpoint.getTasksByProject(0, 0, null);
        assertEquals("response should be bad request", Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
    }

    @Test
    public void testGetTasksByProjectSetsStartPositionIfNotNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasksByProject("5")).thenReturn(query);
        endpoint.getTasksByProject(10, 0, "5");
        verify(taskDao).getTasksByProject("5");
        verify(query).setFirstResult(10);
    }

    @Test
    public void testGetTasksByProjectDoesNotSetStartPositionIfNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasksByProject("5")).thenReturn(query);
        endpoint.getTasksByProject(null, 0, "5");
        verify(taskDao).getTasksByProject("5");
        verify(query, never()).setFirstResult(anyInt());
    }

    @Test
    public void testGetTasksByProjectSetsMaxResultsIfNotNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasksByProject("5")).thenReturn(query);
        endpoint.getTasksByProject(0, 10, "5");
        verify(taskDao).getTasksByProject("5");
        verify(query).setMaxResults(10);
    }

    @Test
    public void testGetTasksByProjectDoesNotSetMaxResultsIfNull() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasksByProject("5")).thenReturn(query);
        endpoint.getTasksByProject(0, null, "5");
        verify(taskDao).getTasksByProject("5");
        verify(query, never()).setMaxResults(anyInt());
    }

    @Test
    public void testGetTasksByProjectReturnsResultListIfProjectNr() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(taskDao.getTasksByProject("5")).thenReturn(query);
        List<Task> list = mock(List.class);
        when(query.getResultList()).thenReturn(list);
        Response result = endpoint.getTasksByProject(null, null, "5");
        assertEquals("response should be OK", Response.Status.OK.getStatusCode(), result.getStatus());
        assertEquals("response should contain the result list", list, result.getEntity());
    }

    @Test
    public void testAddTaskReturnsBadRequestIfProjectIdIsZeroOrNegative() throws Exception {
        task.setProjectId(0);
        Response response = endpoint.addTask(task);
        assertEquals("response should be BAD REQUEST", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        task.setProjectId(-1);
        response = endpoint.addTask(task);
        assertEquals("response should be BAD REQUEST", Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testAddTaskReturnsForbiddenIfUserIsNotManagingProjectManager() throws Exception {
        final long projectId = 5;
        task.setProjectId(projectId);
        Employee employee = mock(ProjectManager.class);
        when(sm.findEmployee()).thenReturn(employee);
        when(taskDao.isManagingProjectManager(projectId, employee)).thenReturn(false);
        Response result = endpoint.addTask(task);
        assertEquals("response should be FORBIDDEN", Response.Status.FORBIDDEN.getStatusCode(), result.getStatus());
    }

    @Test
    public void testAddTaskReturnsBadRequestIfTaskIsInvalid() throws Exception {
        task.setProjectId(5);
        ValidationResult validationResult = new ValidationResult(false);
        when(validator.validate(task, New.class)).thenReturn(validationResult);
        Employee employee = mock(ProjectManager.class);
        when(sm.findEmployee()).thenReturn(employee);
        when(taskDao.isManagingProjectManager(5, employee)).thenReturn(true);
        Response result = endpoint.addTask(task);
        assertEquals("response should be BAD REQUEST", Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        assertEquals("response should be the validation result", validationResult, result.getEntity());
    }

    @Test
    public void testAddTaskReturnsBadRequestIfTaskDaoFails() throws Exception {
        task.setProjectId(5);
        ValidationResult validationResult = new ValidationResult(true);
        when(validator.validate(task, New.class)).thenReturn(validationResult);
        Employee employee = mock(ProjectManager.class);
        when(sm.findEmployee()).thenReturn(employee);
        when(taskDao.isManagingProjectManager(5, employee)).thenReturn(true);
        final String message = "Oops, it failed!";
        EJBTransactionRolledbackException ex = new EJBTransactionRolledbackException("", new IllegalArgumentException(message));

        doThrow(ex).when(taskDao).addTask(task);
        Response result = endpoint.addTask(task);
        assertEquals("response should be BAD_REQUEST", Response.Status.BAD_REQUEST.getStatusCode(), result.getStatus());
        assertEquals("response should be the exception message",
                com.realdolmen.json.Json.error(message),
                result.getEntity()
        );
    }

    @Test
    public void testAddTaskReturnsCreatedOnSucceeded() throws Exception {
        task.setProjectId(5);
        ValidationResult validationResult = new ValidationResult(true);
        when(validator.validate(task, New.class)).thenReturn(validationResult);
        Employee employee = mock(ProjectManager.class);
        when(sm.findEmployee()).thenReturn(employee);
        when(taskDao.isManagingProjectManager(5, employee)).thenReturn(true);
        UriBuilder builder = mock(UriBuilder.class);
        URI uri = new URI("");
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(builder);
        when(builder.path(anyString())).thenReturn(builder);
        when(builder.build(anyVararg())).thenReturn(uri);
        Response response = endpoint.addTask(task);
        assertEquals("response should be CREATED", Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(builder).path(String.valueOf(task.getId()));
        assertEquals("response entity should be absolute URI", uri, response.getLocation());
    }

    @Test
    public void testRemoveTaskDelegatesToTaskDao() throws Exception {
        endpoint.removeTask(5);
        verify(taskDao).removeTask(5);
    }

    @Test
    public void testRemoveTaskReturnsNoContent() throws Exception {
        Response response = endpoint.removeTask(5);
        assertEquals("response should be NO CONTENT", Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }
}