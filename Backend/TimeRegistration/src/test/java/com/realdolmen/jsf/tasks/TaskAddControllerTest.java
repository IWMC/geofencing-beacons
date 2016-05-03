package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.ProjectManager;
import com.realdolmen.entity.Task;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.service.SecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TaskAddControllerTest {

    @Mock
    private TaskDao taskDao;

    @Mock
    private SecurityManager sm;

    @InjectMocks
    private TaskAddController controller = new TaskAddController();

    private Task task = new Task();

    @Before
    public void setUp() throws Exception {
        task.setId(156l);
    }

    @Test
    public void testLoadEntityCreatesNewTaskWithProjectId() throws Exception {
        Task task = controller.loadEntity(15l);
        assertEquals("task should have correct project id", 15l, task.getProjectId());
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
        verify(taskDao, never()).addTask(any());
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
        verify(taskDao, never()).addTask(any());
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
        verify(taskDao, never()).addTask(any());
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
        verify(taskDao).addTask(task);
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

}