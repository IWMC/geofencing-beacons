package com.realdolmen.jsf;

import com.realdolmen.ArquillianTest;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Arquillian test used to test the {@link AuthorizationFilter} used in JSF.
 */
@RunWith(Arquillian.class)
public class AuthorizationFilterTest implements ArquillianTest {

    @Inject
    private AuthorizationFilter authorizationFilter;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testDoFilter() throws Exception {

    }
}