package com.realdolmen.jsf;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.ManagementEmployee;
import com.realdolmen.entity.ProjectManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserContextTest {

    private UserContext userContext = new UserContext();
    private Employee employee = new Employee(1l, 0, "Firstname", "Lastname", "Username", "email@email.com", "hash", "salt", "passw", new HashSet<>());

    @Before
    public void setUp() throws Exception {
        userContext.setEmployee(employee);
    }

    @Test
    public void testGetIsProjectManagerReturnsTrueIfProjectManager() throws Exception {
        ProjectManager manager = new ProjectManager();
        userContext.setEmployee(manager);
        assertTrue(userContext.getIsProjectManager());
    }

    @Test
    public void testGetIsProjectManagerReturnsFalseIfNoProjectManager() throws Exception {
        assertFalse(userContext.getIsProjectManager());
    }

    @Test
    public void testGetIsManagementEmployeeReturnsTrueIfManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();
        userContext.setEmployee(employee);
        assertTrue(userContext.getIsManagementEmployee());
    }

    @Test
    public void testGetIsManagementEmployeeReturnsFalseIfNoManagementEmployee() throws Exception {
        assertFalse(userContext.getIsManagementEmployee());
    }

    @Test
    public void testGetIsManagementReturnsTrueIfManagementEmployee() throws Exception {
        ManagementEmployee employee = new ManagementEmployee();
        userContext.setEmployee(employee);
        assertTrue(userContext.getIsManagement());
    }

    @Test
    public void testGetIsManagementReturnsTrueIfProjectManager() throws Exception {
        ProjectManager employee = new ProjectManager();
        userContext.setEmployee(employee);
        assertTrue(userContext.getIsManagement());
    }

    @Test
    public void testGetIsManagementReturnFalseIfRegularEmployee() throws Exception {
        assertFalse(userContext.getIsManagement());
    }
}