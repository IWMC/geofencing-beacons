package com.realdolmen.jsf;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by BCCAZ45 on 2/03/2016.
 */
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
}