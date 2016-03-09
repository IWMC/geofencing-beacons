package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;
import org.apache.commons.lang3.time.DateUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
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
    public Response getOccupations(@QueryParam("start") @DefaultValue("-1") long start, @QueryParam("end") @DefaultValue("-1") long end) {
        if (start == -1) {
            return Response.status(400).entity("Start date must be filled in").build();
        }

        if (end == -1) {
            end = start;
        }
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(new Date(start));
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date(end));

        startDate.clear(Calendar.HOUR_OF_DAY);
        startDate.clear(Calendar.MINUTE);
        startDate.clear(Calendar.SECOND);
        startDate.clear(Calendar.MILLISECOND);

        endDate.clear(Calendar.HOUR_OF_DAY);
        endDate.clear(Calendar.MINUTE);
        endDate.clear(Calendar.SECOND);
        endDate.clear(Calendar.MILLISECOND);

        if(sm.findEmployee().getId() == null || sm.findEmployee().getId() == 0) {
            return Response.status(400).entity("Bad employee ID: " + sm.findEmployee().getId()).build();
        }

        TypedQuery<RegisteredOccupation> query = em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class);
        query.setParameter("start", startDate).setParameter("employeeId", sm.findEmployee().getId()).setParameter("end", endDate);
        List<RegisteredOccupation> occupations = query.getResultList();
        return Response.ok(occupations).build();
    }
}
