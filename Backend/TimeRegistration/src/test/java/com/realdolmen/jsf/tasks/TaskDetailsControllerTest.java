package com.realdolmen.jsf.tasks;

import com.realdolmen.entity.*;
import com.realdolmen.entity.dao.TaskDao;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.service.SecurityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskDetailsControllerTest {

    @Mock
    private TaskDao taskDao;

    @Mock
    private Language language;

    @Mock
    private SecurityManager sm;

    @InjectMocks
    private TaskDetailsController controller = new TaskDetailsController();

    private Task task;

    private Project project;

    @Before
    public void setUp() throws Exception {
        task = mock(Task.class);
        project = mock(Project.class);
        when(task.getId()).thenReturn(5l);
        when(task.getProject()).thenReturn(project);
        when(project.getId()).thenReturn(55l);
    }

    @Test
    public void testLoadEntityUsesTaskDao() throws Exception {
        controller.loadEntity(task.getId());
        verify(taskDao).findById(task.getId());
    }

    @Test
    public void testLoadEntityInitializesTask() throws Exception {
        when(taskDao.findById(task.getId())).thenReturn(task);
        Task result = controller.loadEntity(task.getId());
        verify(task).initialize();
        assertEquals(task, result);
    }

    @Test
    public void testLoadEntityReturnsNullIfTaskDaoReturnsNull() throws Exception {
        Task result = controller.loadEntity(task.getId());
        assertNull(result);
    }

    @Test
    public void testGetEstimatedHoursShowsSimplifiedViewWhenRoundedNumber() throws Exception {
        Task task = new Task();
        task.setEstimatedHours(7);
        controller.setEntity(task);
        final String text = UUID.randomUUID().toString();
        when(language.getString("project.task.hours", 7)).thenReturn(text);
        String result = controller.getEstimatedHours();
        assertEquals("result should be retrieved with the correct language bundle key", text, result);
    }

    @Test
    public void testGetEstimatedHoursShowsHoursAndMinutesWhenUnroundedNumber() throws Exception {
        Task task = new Task();
        task.setEstimatedHours(7.5);
        controller.setEntity(task);
        final String text = UUID.randomUUID().toString();
        when(language.getString("project.task.hours_minutes", 7, 30)).thenReturn(text);
        String result = controller.getEstimatedHours();
        assertEquals("result should be retrieved with the correct language bundle key", text, result);
    }

    @Test
    public void testRemoveTaskDoesNotRemoveIfManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isManagementEmployee()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.removeTask();
        verify(taskDao, never()).removeTask(any());
    }

    @Test
    public void testRemoveTaskDoesNotRemoveIfRegularEmployee() throws Exception {
        Employee employee = new Employee();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(false);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);

        controller.removeTask();
        verify(taskDao, never()).removeTask(any());
    }

    @Test
    public void testRemoveTaskDoesNotRemoveIfNotManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);
        controller.setEntity(task);

        controller.removeTask();
        verify(taskDao, never()).removeTask(any());
    }

    @Test
    public void testRemoveTaskRemovesWithDaoIfManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);
        controller.setEntity(task);

        controller.removeTask();
        verify(taskDao).removeTask(task);
    }

    @Test
    public void testRemoveTaskRedirectsToProjectDetails() throws Exception {
        ProjectManager employee = new ProjectManager();
        controller.setEntity(task);
        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);
        String response = controller.removeTask();
        assertEquals(Pages.detailsProject(project.getId()).asRedirect(), response);
    }

    @Test
    public void testGetShouldShowEditOptionsReturnsTrueIfUserIsManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(true);

        assertTrue(controller.getShouldShowEditOptions());
    }

    @Test
    public void testGetShouldShowEditOptionsReturnsFalseIfUserNotManagingProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(true);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);

        assertFalse(controller.getShouldShowEditOptions());
    }

    @Test
    public void testGetShouldShowEditOptionsReturnsFalseIfUserIsManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(true);
        when(sm.isProjectManager()).thenReturn(false);
        when(sm.isManagementEmployee()).thenReturn(true);
        when(taskDao.isManagingProjectManager(task, employee)).thenReturn(false);

        assertFalse(controller.getShouldShowEditOptions());
    }

    @Test
    public void testGetShouldShowEditOptionsReturnsFalseIsUserIsRegularEmployee() throws Exception {
        Employee employee = new Employee();
        controller.setEntity(task);

        when(sm.findEmployee()).thenReturn(employee);
        when(sm.isManagement()).thenReturn(false);
        when(sm.isProjectManager()).thenReturn(false);
        when(sm.isManagementEmployee()).thenReturn(false);
        when(taskDao.isManagingProjectManager(project.getId(), employee)).thenReturn(false);

        assertFalse(controller.getShouldShowEditOptions());

    }
}