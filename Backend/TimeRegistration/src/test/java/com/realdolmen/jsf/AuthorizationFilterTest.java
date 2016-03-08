package com.realdolmen.jsf;

import com.realdolmen.WarFactory;
import com.realdolmen.entity.Employee;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

/**
 * Arquillian test used to test the {@link AuthorizationFilter} used in JSF.
 */
@RunWith(Arquillian.class)
public class AuthorizationFilterTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarFactory.createDeployment();
    }

    @Inject
    private AuthorizationFilter authorizationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Inject
    private Session session;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FilterConfig config = mock(FilterConfig.class);
        when(config.getInitParameter(AuthorizationFilter.INCLUDED_WEB_INIT_PARAM))
                .thenReturn("/index.xhtml;/page.xhtml;/range/.*");
        authorizationFilter.init(config);
    }

    private void testPageRedirection() throws Exception {
        verify(response, atLeastOnce()).sendRedirect("/login.xhtml");
    }

    private void testPageNoRedirection() throws Exception {
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    public void testFilterWithoutCredentialsRedirectsWhenRegisteredPage() throws Exception {
        when(request.getRequestURI()).thenReturn("/index.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageRedirection();
        when(request.getRequestURI()).thenReturn("/page.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageRedirection();
    }

    @Test
    public void testFilterWithCredentialsWhenRegisteredPage() throws Exception {
        session.setEmployee(new Employee());
        when(request.getRequestURI()).thenReturn("/index.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageNoRedirection();
        when(request.getRequestURI()).thenReturn("/page.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageNoRedirection();
    }

    @Test
    public void testFilterNoRedirectWithCredentialsWhenUnregisteredPage() throws Exception {
        session.setEmployee(new Employee());
        when(request.getRequestURI()).thenReturn("/unregistered.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageNoRedirection();
    }

    @Test
    public void testFilterNoRedirectWithoutCredentialsWhenUnregisteredPage() throws Exception {
        when(request.getRequestURI()).thenReturn("/unregistered.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        testPageNoRedirection();
    }

    @Test
    public void testChainContinuesWithValidCredentials() throws Exception {
        when(request.getRequestURI()).thenReturn("/unregistered.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        verify(filterChain, atLeastOnce()).doFilter(request, response);
        session.setEmployee(new Employee());
        when(request.getRequestURI()).thenReturn("/index.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        verify(filterChain, atLeastOnce()).doFilter(request, response);
    }

    @Test
    public void testChainStopsWithInvalidCredentials() throws Exception {
        when(request.getRequestURI()).thenReturn("/index.xhtml");
        authorizationFilter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    public void testFilterChecksByRegexMatching() throws Exception {
        when(request.getRequestURI()).thenReturn("/range/somepage");
        authorizationFilter.doFilter(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
    }
}