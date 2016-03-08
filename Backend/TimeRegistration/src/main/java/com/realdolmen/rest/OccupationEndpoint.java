package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
@Stateless
@Path("/occupations")
public class OccupationEndpoint {
    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION_UNIT)
    private EntityManager em;

    @Inject
    private SecurityManager sm;

    @GET
    @Authorized
    @Produces("application/json")
    public Response getOccupations(@QueryParam("start") long start, @QueryParam("end") long end)  {
        Date startDate = new Date(start);
        TypedQuery<RegisteredOccupation> query = em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class);
        query.setParameter("start", startDate).setParameter("employeeId", sm.findEmployee().getId());
        List<RegisteredOccupation> occupations = query.getResultList();
        return Response.ok(occupations).build();
    }
}
