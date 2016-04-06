package com.realdolmen.jsf.occupations;

import com.realdolmen.entity.Occupation;
import com.realdolmen.rest.OccupationEndpoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.faces.context.FacesContext;
import javax.ws.rs.core.Response;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class OccupationDetailControllerTest {

    @Mock
    private FacesContext facesContext;

    private Occupation occupation = new Occupation("Occupation name", "Occupation description");

    @Mock
    private OccupationEndpoint endpoint = new OccupationEndpoint();

    @InjectMocks
    private OccupationDetailController controller = new OccupationDetailController();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        occupation.setId(15l);
        controller.setFacesContext(facesContext);
    }

    @Test
    public void testLoadEntityReturnsNullWhenNoEntityFound() throws Exception {
        Mockito.when(endpoint.findById(occupation.getId())).thenReturn(Response.status(Response.Status.NOT_FOUND).build());
        Occupation occupation = controller.loadEntity(this.occupation.getId());
        Assert.assertNull(occupation);
    }

    @Test
    public void testLoadEntityReturnsEntityWhenFound() throws Exception {
        Mockito.when(endpoint.findById(occupation.getId())).thenReturn(Response.ok(occupation).build());
        Occupation occupation = controller.loadEntity(this.occupation.getId());
        Assert.assertEquals("response should return the correct entity", this.occupation, occupation);
    }

    @Test
    public void testDetailControllerIsSerializable() throws Exception {
        ObjectOutputStream out = new ObjectOutputStream(mock(OutputStream.class));

        try {
            out.writeObject(new OccupationDetailController());
        } catch (NotSerializableException nsex) {
            fail("OccupationDetailController is not serializable");
        }
    }
}
