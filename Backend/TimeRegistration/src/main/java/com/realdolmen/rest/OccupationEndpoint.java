package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.DateUtil;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.joda.time.DateTimeFieldType.*;

@SuppressWarnings("JpaQueryApiInspection")
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
    @Authorized(UserGroup.MANAGEMENT)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response listAll(@QueryParam("start") Integer startPosition,
                            @QueryParam("max") Integer maxResult) {
        TypedQuery<Occupation> findAllQuery = em.createNamedQuery("Occupation.findAll", Occupation.class);

        if (startPosition != null) {
            findAllQuery.setFirstResult(startPosition);
        }

        if (maxResult != null) {
            findAllQuery.setMaxResults(maxResult);
        }

        List<Occupation> occupations = findAllQuery.getResultList();
        occupations.forEach(Occupation::initialize);
        return Response.ok().entity(occupations).build();
    }

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
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableOccupations() {
        TypedQuery<Occupation> query = em.createNamedQuery("Occupation.findAvailableByEmployee", Occupation.class);
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

        return Response.created(URI.create("/" + ro.getId())).entity(
                Json.createObjectBuilder().add("id", ro.getId()).build()
        ).build();
    }

    @DELETE
    @Path("/registration/{id:[0-9][0-9]*}")
    @Authorized
    @Transactional
    public Response removeRegisteredOccupation(@PathParam("id") long id) {
        RegisteredOccupation ro = (RegisteredOccupation) em.createNamedQuery("RegisteredOccupation.findOccupationByIdAndUser").setParameter("regId", id).setParameter("userId", sm.findEmployee().getId()).getSingleResult();
        if (ro != null) {
            em.remove(ro);
            return Response.ok().build();
        }
        return Response.notModified().build();
    }

    @POST
    @Path("/{id:[0-9][0-9]*}/points")
    @Authorized(UserGroup.PROJECT_MANAGER_ONLY)
    @Transactional
    public Response addLocationPoint(@PathParam("id") Long id, JsonObject point) {
        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (point == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Project project = em.find(Project.class, id);

        if (project == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        String longitude = point.getString("long", "");
        String latitude = point.getString("lat", "");

        if (longitude.isEmpty() || latitude.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Location location = null;

        try {
            location = new Location(Double.parseDouble(latitude), Double.parseDouble(longitude));
            project.getLocations().add(location);
        } catch (NumberFormatException nfex) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (location != null) {
            em.persist(location);
            em.merge(project);
        }

        return Response.noContent().build();
    }
}
