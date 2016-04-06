package com.realdolmen.jsf;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.primefaces.material.application.ToastService;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DetailControllerTest implements Serializable {

    private Serializable entity;
    private Pages.Page errorPage;

    @Mock
    private transient FacesContext facesContext;

    @Mock
    private transient ExternalContext externalContext;

    @Mock
    private transient ToastService toastService;

    private class DetailControllerImpl extends DetailController implements Serializable {

        public DetailControllerImpl() {
            super(errorPage);
            setToastService(toastService);
            setFacesContext(facesContext);
        }

        @Override
        public Serializable loadEntity(long id) {
            return entity;
        }
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }

    private void verifyRedirection() throws IOException {
        verify(externalContext, atLeastOnce()).redirect(errorPage.asLocationRedirect());
    }

    @Test
    public void testRedirectToErrorPageRedirectsToErrorPageWhenRequestIsNotCommitted() throws Exception {
        errorPage = Pages.searchOccupation();
        DetailController controller = new DetailControllerImpl();
        controller.redirectToErrorPage();
        verifyRedirection();
    }

    @Test
    public void testRedirectToErrorPageRedirectsToErrorPageWhenRequestIsCommitted() throws Exception {
        errorPage = Pages.searchOccupation();
        DetailController controller = new DetailControllerImpl();
        when(externalContext.isResponseCommitted()).thenReturn(true);
        controller.redirectToErrorPage();
        verify(externalContext, never()).redirect(errorPage.asLocationRedirect());
    }

    @Test
    public void testOnPreRenderSetsEntityWithValidEntity() throws Exception {
        entity = "The entity";
        DetailController controller = new DetailControllerImpl();
        controller.setId("1");
        controller.onPreRender();
        assertEquals("correct entity should be set", entity, controller.getEntity());
    }

    @Test
    public void testOnPreRenderRedirectsWithInvalidEntity() throws Exception {
        entity = null;
        errorPage = Pages.searchOccupation();
        DetailController controller = new DetailControllerImpl();
        controller.setId("1");
        controller.onPreRender();
        verifyRedirection();
    }

    @Test
    public void testOnPreRenderRedirectsWithInvalidEntityId() throws Exception {
        entity = null;
        errorPage = Pages.searchOccupation();
        DetailController controller = new DetailControllerImpl();
        controller.setId(null);
        controller.onPreRender();
        verifyRedirection();
        controller.setId("");
        controller.onPreRender();
        verify(externalContext, atLeast(2)).redirect(errorPage.asLocationRedirect());
        controller.setId("a");
        controller.onPreRender();
        verify(externalContext, atLeast(3)).redirect(errorPage.asLocationRedirect());
    }

    @Test
    public void testDetailControllerIsSerializable() throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(mock(OutputStream.class));
        errorPage = Pages.searchOccupation();

        try {
            out.writeObject(new DetailControllerImpl());
        } catch (NotSerializableException nsex) {
            fail("DetailController is not serializable");
        }
    }
}