package com.realdolmen.rest;

import com.realdolmen.entity.Employee;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.Validator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;

import java.security.NoSuchAlgorithmException;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserEndpointTest {

    @Mock private SecurityManager securityManager;
    @Mock private Validator<Employee> employeeValidator;
    @Mock private EntityManager entityManager;
    @InjectMocks private UserEndpoint endpoint;
    private Employee employee;

    @Before
    public void setUp() throws Exception {
        when(securityManager.randomSalt()).thenReturn("1234567");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRegister() throws Exception {

    }

    @Test
    public void testLogin() throws Exception {

    }
}