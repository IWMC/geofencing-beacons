package com.realdolmen.jsf;

import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.Project;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PagesTest {

    @Test
    public void testIndexWithNoParameterProducesCorrectRedirect() {
        assertEquals("Should be a redirect with no parameters", "/index.xhtml?faces-redirect=true", Pages.index().asRedirect());
    }

    @Test
    public void testIndexWithNoParameterAndNoRedirectProducesNoRedirect() {
        assertEquals("Should be just the baseUrl", "/index.xhtml", Pages.index().asLocationRedirect());
    }

    @Test
    public void testIndexWithParameterRedirectsCorrectly() {
        assertEquals("Should be a redirect with parameters",
                "/index.xhtml?test=test&faces-redirect=true", Pages.index().param("test", "test").asRedirect());
    }

    @Test
    public void testIndexWithParameterWithoutRedirect() {
        assertEquals("Should be the baseUrl with the parameter",
                "/index.xhtml?test=test", Pages.index().param("test", "test").asLocationRedirect());
    }

    @Test
    public void testIndexWithMultipleParametersRedirects() {
        assertEquals("Should be a redirect with every parameter",
                "/index.xhtml?other=test&test=test&faces-redirect=true", Pages.index().param("test", "test").param("other", "test").asRedirect());
    }

    @Test
    public void testIndexWithMultipleParametersWithoutRedirect() {
        assertEquals("Should be the baseUrl with every parameter",
                "/index.xhtml?other=test&test=test", Pages.index().param("test", "test").param("other", "test").asLocationRedirect());
    }

    @Test
    public void testOccupationDetailsFromOccupationReturnsCorrectLink() throws Exception {
        Project project = new Project();
        Occupation occupation = new Occupation();
        assertEquals("should link to occupation URL", Pages.detailsOccupation().param("id", String.valueOf(occupation.getId())),
                Pages.occupationDetailsFrom(occupation));
        assertEquals("should link to project URL", Pages.detailsProject().param("id", String.valueOf(project.getId())),
                Pages.occupationDetailsFrom(project));
    }
}