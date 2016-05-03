package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.*;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskEditControllerTest {

    @Mock
    private SecurityManager sm;

    @Mock
    private TaskDao taskDao;

    @InjectMocks
    private TaskEditController controller = new TaskEditController();

    private Task task;

    private Project project;

    private Employee employee = new Employee();

    @Before
    public void setUp() throws Exception {
        task = mock(Task.class);
        project = mock(Project.class);

        when(task.getId()).thenReturn(5l);
        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(56l);
        when(task.getEmployees()).thenReturn(new HashSet<>());
        when(project.getEmployees()).thenReturn(new HashSet<>());

        employee.setId(123l);
        project.getEmployees().add(employee);
        task.getEmployees().add(employee);
    }

    @Test
    public void testSaveTaskDoesNotSaveIfManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isManagementEmployee()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.saveTask();
        verify(taskDao, never()).update(any());
    }

    @Test
    public void testSaveTaskDoesNotSaveIfRegularEmployee() throws Exception {
        Employee employee = new Employee();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(false);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);

        controller.saveTask();
        verify(taskDao, never()).update(any());
    }

    @Test
    public void testSaveTaskDoesNotSaveIfNotManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.saveTask();
        verify(taskDao, never()).update(any());
    }

    @Test
    public void testSaveTaskSavesWithDaoIfManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);
        controller.setEntity(task);

        controller.saveTask();
        verify(taskDao).update(task);
    }

    @Test
    public void testSaveTaskRedirectsToProjectDetails() throws Exception {
        ProjectManager employee = new ProjectManager();
        controller.setEntity(task);
        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);
        String response = controller.saveTask();
        assertEquals(Pages.detailsTask(task.getId()).asRedirect(), response);
    }

    @Test
    public void testRemoveEmployeeDoesNotSaveIfManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isManagementEmployee()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.removeEmployee(this.employee);
        verify(taskDao, never()).update(any());
        assertEquals("task should still contain one employee", 1, task.getEmployees().size());
        assertEquals("task should still contain the correct employee", this.employee, task.getEmployees().iterator().next());
    }

    @Test
    public void testRemoveEmployeeDoesNotSaveIfRegularEmployee() throws Exception {
        Employee employee = new Employee();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(false);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);

        controller.removeEmployee(this.employee);
        verify(taskDao, never()).update(any());
        assertEquals("task should still contain one employee", 1, task.getEmployees().size());
        assertEquals("task should still contain the correct employee", this.employee, task.getEmployees().iterator().next());
    }

    @Test
    public void testRemoveEmployeeDoesNotSaveIfNotManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.removeEmployee(this.employee);
        verify(taskDao, never()).update(any());
        assertEquals("task should still contain one employee", 1, task.getEmployees().size());
        assertEquals("task should still contain the correct employee", this.employee, task.getEmployees().iterator().next());
    }

    @Test
    public void testRemoveEmployeeSavesWithDaoIfManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);
        controller.setEntity(task);

        controller.removeEmployee(this.employee);
        verify(taskDao).update(task);
        assertEquals("task should contain zero employees", 0, task.getEmployees().size());
    }
}