package com.realdolmen.jsf;

import com.realdolmen.TestMode;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.primefaces.material.application.ToastService;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import static org.mockito.Mockito.when;

public class ControllerTest {

    @Mock
    private ToastService toastService;

    @Mock
    private FacesContext facesContext;

    @Mock
    private ExternalContext externalContext;

    public ControllerTest() {
        TestMode.enableTestMode();
        MockitoAnnotations.initMocks(this);
    }

    public void initForController(Controller controller) {
        controller.setFacesContext(getFacesContext());
        controller.setToastService(getToastService());
        when(facesContext.getExternalContext()).thenReturn(externalContext);
    }

    public ToastService getToastService() {
        return toastService;
    }

    public void setToastService(ToastService toastService) {
        this.toastService = toastService;
    }

    public FacesContext getFacesContext() {
        return facesContext;
    }

    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }

    public ExternalContext getExternalContext() {
        return externalContext;
    }

    public void setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
    }
}