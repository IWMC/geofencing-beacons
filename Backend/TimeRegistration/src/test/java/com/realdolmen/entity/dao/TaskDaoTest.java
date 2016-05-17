package com.realdolmen.entity.dao;

import com.realdolmen.entity.*;
import com.realdolmen.entity.validation.Existing;
import com.realdolmen.entity.validation.New;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDaoTest {

    @Mock
    private EntityManager em;

    @Mock
    private Validator<Task> taskValidator;

    @InjectMocks
    private TaskDao taskDao = new TaskDao();

    private Task task = new Task();

    private Project project = new Project();
    private Employee employee1 = mock(Employee.class);
    private Employee employee2 = mock(Employee.class);
    private ProjectManager projectManager = new ProjectManager();

    @Before
    public void setUp() throws Exception {
        task.setId(10l);
        projectManager.setId(11l);
        project.setId(156l);
        when(em.find(Task.class, task.getId())).thenReturn(task);
    }

    private void setTaskProjectId() {
        task.setProjectId(1l);
        when(em.find(Project.class, 1l)).thenReturn(project);
        when(taskValidator.validate(eq(task), anyVararg())).thenReturn(new ValidationResult(true));
    }

    private void addEmployeeIds() {
        final long id1 = 5, id2 = 6;
        when(em.find(Employee.class, id1)).thenReturn(employee1);
        when(em.find(Employee.class, id2)).thenReturn(employee2);
        when(employee1.getId()).thenReturn(id1);
        when(employee2.getId()).thenReturn(id2);
        task.getEmployeeIds().add(id1);
        task.getEmployeeIds().add(id2);
    }

    @Test
    public void testFindByIdDelegatesToEntityManager() throws Exception {
        Task result = taskDao.findById(task.getId());
        verify(em).find(Task.class, task.getId());
        assertEquals("result should be the same as the task from the database", task, result);
    }

    @Test
    public void testFindByIdEagerlyFindsCorrectTask() throws Exception {
        Task result = taskDao.findById(task.getId());
        assertEquals("result should be the same as the task from the database", task, result);
    }

    @Test
    public void testFindReferenceByIdReturnsALazyObject() throws Exception {
        Task lazyTask = mock(Task.class);
        when(em.getReference(Task.class, task.getId())).thenReturn(lazyTask);
        Task result = taskDao.findReferenceById(task.getId());
        assertEquals("result should be the same as the task from the database", lazyTask, result);
        verify(em, never()).find(Task.class, task.getId());
        verify(em).getReference(Task.class, task.getId());
    }

    @Test
    public void testGetTasksUsesNamedQuery() throws Exception {
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(em.createNamedQuery("Task.findAll", Task.class)).thenReturn(query);
        TypedQuery<Task> result = taskDao.getTasks();
        verify(em).createNamedQuery("Task.findAll", Task.class);
        assertEquals(query, result);
    }

    @Test
    public void testGetAllTasksDoesNotFilterList() throws Exception {
        List<Task> tasks = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> tasks.add(mock(Task.class)));
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(em.createNamedQuery("Task.findAll", Task.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(tasks);
        List<Task> result = taskDao.getAllTasks();
        verify(em).createNamedQuery("Task.findAll", Task.class);
        verify(query).getResultList();
        assertEquals(tasks, result);
    }

    @Test
    public void testGetTasksByProjectUsesNamedQuery() throws Exception {
        final String id = "10";
        TypedQuery<Task> query = mock(TypedQuery.class);
        when(em.createNamedQuery("Task.findByProjectId", Task.class)).thenReturn(query);
        when(query.setParameter(anyString(), anyObject())).thenReturn(query);
        TypedQuery<Task> result = taskDao.getTasksByProject(id);
        verify(em).createNamedQuery("Task.findByProjectId", Task.class);
        verify(query).setParameter("projectId", id);
        assertEquals(result, query);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTaskThrowsExceptionWhenNoProjectIdOrProject() throws Exception {
        taskDao.addTask(task);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTaskThrowsExceptionWhenInvalidProjectId() throws Exception {
        task.setProjectId(5l);
        taskDao.addTask(task);
    }

    @Test
    public void testAddTaskSetsProjectAsRelationship() throws Exception {
        setTaskProjectId();
        taskDao.addTask(task);
        verify(em).find(Project.class, task.getProjectId());
        assertEquals(project, task.getProject());
    }

    @Test
    public void testAddTaskRetrievesEmployeesByIdAndAddsToRelationship() throws Exception {
        setTaskProjectId();
        addEmployeeIds();
        taskDao.addTask(task);
        verify(em).find(Employee.class, employee1.getId());
        verify(em).find(Employee.class, employee2.getId());
        assertEquals("2 employees should be bound to the task", 2, task.getEmployees().size());
        assertEquals("task should contain the database employees",
                new HashSet<>(Arrays.asList(employee1, employee2)), task.getEmployees());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTaskThrowsExceptionWhenInvalidEmployeeId() throws Exception {
        setTaskProjectId();
        addEmployeeIds();
        when(em.find(Employee.class, employee1.getId())).thenReturn(null);
        taskDao.addTask(task);
    }

    @Test
    public void testAddTaskValidatesTask() throws Exception {
        setTaskProjectId();
        taskDao.addTask(task);
        verify(taskValidator).validate(task, New.class);
    }

    @Test
    public void testAddTaskPersistsTaskWhenValid() throws Exception {
        setTaskProjectId();
        taskDao.addTask(task);
        verify(em).persist(task);
    }

    @Test
    public void testAddTaskDoesNotPeristTaskWhenInvalid() throws Exception {
        setTaskProjectId();
        when(taskValidator.validate(task, New.class)).thenReturn(new ValidationResult(false, Arrays.asList("test")));
        taskDao.addTask(task);
        verify(em, never()).persist(task);
    }

    @Test
    public void testRemoveTaskRemovesInstanceWhenManaged() throws Exception {
        when(em.contains(task)).thenReturn(true);
        taskDao.removeTask(task);
        verify(em).remove(task);
    }

    @Test
    public void testRemoveTaskRemovesByNamedQueryWhenNotManaged() throws Exception {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("Task.deleteById")).thenReturn(query);
        taskDao.removeTask(task);
        verify(em, never()).remove(task);
        verify(query).setParameter("id", task.getId());
        verify(query).executeUpdate();
    }

    @Test
    public void testRefreshFindsTaskIfNotManaged() throws Exception {
        Task mock = mock(Task.class);
        when(em.find(Task.class, task.getId())).thenReturn(mock);
        Task result = taskDao.refresh(task);
        verify(em).find(Task.class, task.getId());
        assertEquals("result should be the database task", mock, result);
    }

    @Test
    public void testRefreshRefreshesWhenManaged() throws Exception {
        when(em.contains(task)).thenReturn(true);
        taskDao.refresh(task);
        verify(em).refresh(task);
        verify(em, never()).find(Task.class, task.getId());
    }

    @Test
    public void testRefreshWithReferenceFindsTaskIfNotManaged() throws Exception {
        Task mock = mock(Task.class);
        when(em.getReference(Task.class, task.getId())).thenReturn(mock);
        Task result = taskDao.refreshWithReference(task);
        verify(em, never()).find(Task.class, task.getId());
        verify(em).getReference(Task.class, task.getId());
        assertEquals("result should be the database task", mock, result);
    }

    @Test
    public void testRefreshWithReferenceRefreshesWhenManaged() throws Exception {
        when(em.contains(task)).thenReturn(true);
        taskDao.refresh(task);
        verify(em).refresh(task);
        verify(em, never()).getReference(Task.class, task.getId());
    }

    @Test
    public void removeTaskWithIdCallsNamedQueryWithId() throws Exception {
        Query query = mock(Query.class);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(em.createNamedQuery("Task.deleteById")).thenReturn(query);
        taskDao.removeTask(task.getId());
        verify(query).setParameter("id", task.getId());
        verify(query).executeUpdate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNonExistingTaskThrowsException() throws Exception {
        when(taskValidator.validate(task, Existing.class)).thenReturn(new ValidationResult(true));
        when(em.merge(task)).thenThrow(IllegalArgumentException.class);
        taskDao.update(task);
    }

    @Test
    public void testUpdateExistingTaskMergesWhenValid() throws Exception {
        when(taskValidator.validate(task, Existing.class)).thenReturn(new ValidationResult(true));
        taskDao.update(task);
        verify(em).merge(task);
    }

    @Test
    public void testUpdateExistingTaskDoesNotMergeWhenInvalid() throws Exception {
        when(taskValidator.validate(task, Existing.class)).thenReturn(new ValidationResult(false));
        taskDao.update(task);
        verify(em, never()).merge(task);
    }

    @Test
    public void testUpdateTaskReturnsCorrectValidationResult() throws Exception {
        ValidationResult validationResult = new ValidationResult(false, Arrays.asList("test", "test.empty"));
        when(taskValidator.validate(task, Existing.class)).thenReturn(validationResult);
        ValidationResult daoResult = taskDao.update(task);
        assertEquals("result should be the correct validation result", validationResult, daoResult);
    }

    @Test
    public void testIsManagingProjectManagerReturnFalseWhenNull() throws Exception {
        boolean result = taskDao.isManagingProjectManager(task, null);
        assertFalse(result);
    }

    @Test
    public void testIsManagingProjectManagerReturnsFalseWhenNoProjectManager() throws Exception {
        Employee regularEmployee = mock(Employee.class);
        ManagementEmployee managementEmployee = mock(ManagementEmployee.class);
        task.getEmployees().add(regularEmployee);
        task.getEmployees().add(managementEmployee);
        assertFalse(taskDao.isManagingProjectManager(task, regularEmployee));
        assertFalse(taskDao.isManagingProjectManager(task, managementEmployee));
    }

    @Test
    public void testIsManagingProjectManagerReturnsFalseWhenNotContainedInEmployeeList() throws Exception {
        when(em.getReference(Task.class, task.getId())).thenReturn(task);
        task.setProject(project);
        assertFalse(taskDao.isManagingProjectManager(task, projectManager));
    }

    @Test
    public void testIsManagingProjectManagerReturnsTrueWhenContainedInEmployeeList() throws Exception {
        when(em.getReference(Task.class, task.getId())).thenReturn(task);
        task.setProject(project);
        project.getEmployees().add(projectManager);
        assertTrue(taskDao.isManagingProjectManager(task, projectManager));
    }

    @Test
    public void testIsManagingProjectManagerByIdReturnsFalseWhenNull() throws Exception {
        boolean result = taskDao.isManagingProjectManager(task, null);
        assertFalse(result);
    }

    @Test
    public void testIsManagingProjectManagerByIdReturnsFalseWhenNoProjectManager() throws Exception {
        Employee regularEmployee = mock(Employee.class);
        ManagementEmployee managementEmployee = mock(ManagementEmployee.class);
        task.setProject(project);
        task.getEmployees().add(regularEmployee);
        task.getEmployees().add(managementEmployee);
        assertFalse(taskDao.isManagingProjectManager(task.getProjectId(), regularEmployee));
        assertFalse(taskDao.isManagingProjectManager(task.getProjectId(), managementEmployee));
    }

    @Test
    public void testIsManagingProjectManagerByIdReturnsFalseWhenNotContainedInEmployeeList() throws Exception {
        when(em.getReference(Project.class, project.getId())).thenReturn(project);
        task.setProject(project);
        assertFalse(taskDao.isManagingProjectManager(project.getId(), projectManager));
    }

    @Test
    public void testIsManagingProjectManagerByIdReturnsTrueWhenContainedInEmployeeList() throws Exception {
        when(em.getReference(Project.class, project.getId())).thenReturn(project);
        task.setProject(project);
        project.getEmployees().add(projectManager);
        assertTrue(taskDao.isManagingProjectManager(project.getId(), projectManager));
    }
}