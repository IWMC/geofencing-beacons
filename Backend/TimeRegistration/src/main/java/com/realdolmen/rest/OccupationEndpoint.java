package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.DateUtil;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.joda.time.DateTimeFieldType.*;

@Stateless
@Path("/occupations")
public class OccupationEndpoint {

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Inject
    private SecurityManager sm;

    @Inject
    private Validator<RegisteredOccupation> regOccValidator;

    public static final long MINIMUM_EPOCH = DateTime.now().minusYears(1).getMillis();

    @GET
    @Path("registration")
    @Authorized
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisteredOccupations(@QueryParam("date") @DefaultValue("-1") long date) {
        if (date <= MINIMUM_EPOCH) {
            return Response.status(400).build();
        }


        LocalDateTime startDate = new DateTime(date, DateTimeZone.UTC).withHourOfDay(0).withMinuteOfHour(0).toLocalDateTime();
        System.out.println(startDate + " in zone: " + startDate.toDateTime().getZone());
        if (sm.findEmployee() == null || sm.findEmployee().getId() == null || sm.findEmployee().getId() == 0) {
            return Response.status(400).build();
        }

        //TODO: take into account timezone differences with the phone and the server

        TypedQuery<RegisteredOccupation> query = em.createNamedQuery("RegisteredOccupation.findOccupationsInRange", RegisteredOccupation.class);
        System.out.printf("Performing query: Start time: %d -> %s, employee id: %d%n", startDate.toDateTime().getMillis(), startDate.toDateTime().toString(), sm.findEmployee().getId());
        query
                .setParameter("employeeId", sm.findEmployee().getId())
                .setParameter("year", startDate.get(year()))
                .setParameter("month", startDate.get(monthOfYear()))
                .setParameter("day", startDate.get(dayOfMonth()));

        List<RegisteredOccupation> occupations = query.getResultList();
        occupations.forEach(ro -> {
            ro.setRegisteredStart(
                    DateUtil.toUTC(new DateTime(ro.getRegisteredStart())).toDate()
            );

            ro.setRegisteredEnd(
                    DateUtil.toUTC(new DateTime(ro.getRegisteredEnd())).toDate()
            );
        });

        occupations.forEach(RegisteredOccupation::initialize);

        return Response.ok(occupations).build();
    }

    @PUT
    @Path("registration/{date}/confirm")
    @Transactional
    @Authorized
    public Response confirmOccupationRegistration(@PathParam("date") long date) {
        List<RegisteredOccupation> occupations = (List<RegisteredOccupation>) getRegisteredOccupations(date).getEntity();
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

    @GET
    @Path("registration/range")
    @Authorized
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisteredOccupationsOfLastXDays(@QueryParam("date") @DefaultValue("-1") long date, @QueryParam("count") @DefaultValue("7") int count) {
        List<RegisteredOccupation> occupations = new ArrayList<>();
        DateTime time = new DateTime(date, DateTimeZone.UTC);
        if (count <= 0) {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            occupations.addAll((List<RegisteredOccupation>) getRegisteredOccupations(time.minusDays(i).getMillis()).getEntity());
        }
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
        em.persist(ro);
        foundEmployee.getRegisteredOccupations().add(ro);


        return Response.created(URI.create("/" + ro.getId())).build();
    }
}
