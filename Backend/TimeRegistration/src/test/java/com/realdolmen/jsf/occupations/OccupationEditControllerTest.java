package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.jsf.ControllerTest;
import com.realdolmen.jsf.Pages;
import com.realdolmen.messages.Language;
import com.realdolmen.rest.OccupationEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class OccupationEditControllerTest extends ControllerTest {

    @Mock
    private OccupationEndpoint endpoint;

    @Mock
    private Language language;

    private Occupation occupation = new Occupation("name", "description");

    @InjectMocks
    private OccupationEditController controller = new OccupationEditController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initForController(controller);
        occupation.setId(10l);
        controller.setEntity(occupation);
        when(endpoint.update(occupation.getId(), occupation)).thenReturn(Response.noContent().build());
    }

    @Test
    public void testSaveOccupationCallsOccupationEndpoint() throws Exception {
        controller.saveOccupation();
        verify(endpoint, atLeastOnce()).update(occupation.getId(), occupation);
    }

    @Test
    public void testSaveOccupationRedirectsToOccupationDetailsOnSuccess() throws Exception {
        controller.saveOccupation();
        verify(getExternalContext(), atLeastOnce()).redirect(Pages.detailsOccupation().param("id", occupation.getId())
                .asLocationRedirect());
    }

    @Test
    public void testSaveOccupationShowsToastOnFailure() throws Exception {
        String message = UUID.randomUUID().toString();
        when(endpoint.update(occupation.getId(), occupation)).thenReturn(Response.status(Response.Status.BAD_REQUEST).build());
        when(language.getString("occupation.name_taken")).thenReturn(message);
        controller.saveOccupation();
        verify(getToastService(), atLeastOnce()).newToast(eq(message), anyInt());
    }
}