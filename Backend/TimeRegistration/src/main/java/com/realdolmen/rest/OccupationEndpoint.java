package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.Employee;
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
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.Instant;
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
            return Response.status(400).build();
        }

        if (end <= MINIMUM_EPOCH) {
            end = start;
        }
        Date startDate = Date.from(Instant.ofEpochMilli(start));
        Date endDate = Date.from(Instant.ofEpochMilli(end));

        if (sm.findEmployee() == null || sm.findEmployee().getId() == null || sm.findEmployee().getId() == 0) {
            return Response.status(400).build();
        }

        //TODO: take into account timezone differences with the phone and the server

        TypedQuery<RegisteredOccupation> query = em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class);
        System.out.printf("Performing query: Start time: %d, end time: %d, employee id: %d%n", startDate.getTime(), endDate.getTime(), sm.findEmployee().getId());
        query.setParameter("start", startDate, TemporalType.DATE).setParameter("employeeId", sm.findEmployee().getId()).setParameter("end", endDate, TemporalType.DATE);
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

        Employee foundEmployee = em.find(Employee.class, ro.getRegistrar().getId());
        ro.setRegistrar(foundEmployee);

        foundEmployee.getRegisteredOccupations().add(ro);

        em.persist(ro);

        return Response.created(URI.create("/" + ro.getId())).build();
    }
}
