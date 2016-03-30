package com.realdolmen.jsf;

import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.Project;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PagesTest {

    @Test
    public void testIndexWithNoParameterProducesCorrectRedirect() {
        assertEquals("Should be a redirect with no parameters", "/index.xhtml?faces-redirect=true", Pages.index().redirect());
    }

    @Test
    public void testIndexWithNoParameterAndNoRedirectProducesNoRedirect() {
        assertEquals("Should be just the baseUrl", "/index.xhtml", Pages.index().noRedirect());
    }

    @Test
    public void testIndexWithParameterRedirectsCorrectly() {
        assertEquals("Should be a redirect with parameters",
                "/index.xhtml?test=test&faces-redirect=true", Pages.index().param("test", "test").redirect());
    }

    @Test
    public void testIndexWithParameterWithoutRedirect() {
        assertEquals("Should be the baseUrl with the parameter",
                "/index.xhtml?test=test", Pages.index().param("test", "test").noRedirect());
    }

    @Test
    public void testIndexWithMultipleParametersRedirects() {
        assertEquals("Should be a redirect with every parameter",
                "/index.xhtml?other=test&test=test&faces-redirect=true", Pages.index().param("test", "test").param("other", "test").redirect());
    }

    @Test
    public void testIndexWithMultipleParametersWithoutRedirect() {
        assertEquals("Should be the baseUrl with every parameter",
                "/index.xhtml?other=test&test=test", Pages.index().param("test", "test").param("other", "test").noRedirect());
    }

    @Test
    public void testOccupationDetailsFromOccupationReturnsCorrectLink() throws Exception {
        Project project = new Project();
        Occupation occupation = new Occupation();
        assertEquals("should link to occupation URL", Pages.detailsOccupation(), Pages.occupationDetailsFrom(occupation));
        assertEquals("should link to project URL", Pages.detailsProject(), Pages.occupationDetailsFrom(project));
    }
}