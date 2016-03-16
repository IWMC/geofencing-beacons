package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
@Path("/occupations")
public class OccupationEndpoint {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private SecurityManager sm;

    @Inject
    private Validator<RegisteredOccupation> regOccValidator;

    public static final long MINIMUM_EPOCH;

    static {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2015);
        MINIMUM_EPOCH = c.getTime().getTime();
    }

    @GET
    @Path("registration")
    @Authorized
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisteredOccupations(@QueryParam("start") @DefaultValue("-1") long start, @QueryParam("end") @DefaultValue("-1") long end) {
        if (start <= MINIMUM_EPOCH) {
            return Response.status(400).entity("Start date must be after " + MINIMUM_EPOCH + " but is " + start).build();
        }

        if (end <= MINIMUM_EPOCH) {
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

        if (sm.findEmployee() == null || sm.findEmployee().getId() == null || sm.findEmployee().getId() == 0) {
            return Response.status(400).entity("Bad employee ID: " + sm.findEmployee() != null ? sm.findEmployee().getId() : "Unknown").build();
        }

        //TODO: take into account timezone differences with the phone and the server

        TypedQuery<RegisteredOccupation> query = em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class);
        query.setParameter("start", startDate.getTime()).setParameter("employeeId", sm.findEmployee().getId()).setParameter("end", endDate.getTime());
        List<RegisteredOccupation> occupations = query.getResultList();
        return Response.ok(occupations).build();
    }

    @PUT
    @Path("registration/{date}/confirm")
    @Transactional
    @Authorized
    public Response confirmOccupationRegistration(@PathParam("date") long date) {
        List<RegisteredOccupation> occupations = (List<RegisteredOccupation>) getRegisteredOccupations(date, date).getEntity();
        occupations.forEach(o -> {
            em.detach(o);
            o.confirm();
        });

        if (occupations.stream().anyMatch(o -> !o.isConfirmed())) {
            return Response.notModified().build();
        }

        occupations.forEach(em::merge);
        return Response.ok().build();
    }

    @GET
    @Authorized
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableOccupations() {
        TypedQuery<Occupation> query = em.createNamedQuery("Occupation.FindAvailableByEmployee", Occupation.class);
        List<Occupation> occupations = query.getResultList();
        occupations.forEach(Occupation::initialize);
        return Response.ok(occupations).build();
    }

    @POST
    @Transactional
    @Path("registration")
    @Authorized
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addOccupationRegistration(RegisteredOccupation ro) {

        ValidationResult validationResult = regOccValidator.validate(ro);
        if (!validationResult.isValid())
            return Response.status(400).entity(validationResult.getInvalidationTokens()).build();

        ro.getRegistrar().getRegisteredOccupations().add(ro);
        em.persist(ro);
        return Response.created(URI.create("/" + ro.getId())).build();
    }
}
