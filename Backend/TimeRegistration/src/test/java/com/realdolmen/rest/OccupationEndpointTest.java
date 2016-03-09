package com.realdolmen.rest;

import com.realdolmen.ArquillianUtil;
import com.realdolmen.jsf.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.realdolmen.service.SecurityManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static org.junit.Assert.*;

/**
 * Created by BCCAZ45 on 9/03/2016.
 */
@RunWith(Arquillian.class)
public class OccupationEndpointTest {

    @Inject
    private EntityManager em;

    @InjectMocks
    private OccupationEndpoint endpoint;

    @Mock
    private SecurityManager sm;

    @Inject
    private Session session;

    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianUtil.createDeployment();
    }


    @Before
    public void setUp() throws Exception {
        endpoint = new OccupationEndpoint();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetOccupationsWithStartDateOnlyReturnsAllOccupationsOfThatDay() {

    }

    @Test
    public void testGetOccupationsWithEndDateOnlyReturnsBadRequest() {

    }

    @Test
    public void testGetOccupationsWithoutAuthenticationIsForbidden() {

    }

    @Test
    public void testGetOccupationsWithStartAndEndDateReturnsCorrectList() {

    }

    @Test
    public void testGetOccupationsWithBadEmployeeReturnsBadRequest() {

    }
}