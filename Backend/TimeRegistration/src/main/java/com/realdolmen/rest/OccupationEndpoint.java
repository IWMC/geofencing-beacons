package com.realdolmen.rest;

import com.realdolmen.annotations.Authorized;
import com.realdolmen.annotations.UserGroup;
import com.realdolmen.entity.*;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.ValidationResult;
import com.realdolmen.validation.Validator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
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
    @Path("/{id:[0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response findById(Long id) {
        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Occupation occupation = em.find(Occupation.class, id);
        if (occupation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(occupation).build();
        }
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
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableOccupations() {
        TypedQuery<Occupation> query = em.createNamedQuery("Occupation.findAvailableByEmployee", Occupation.class);
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

    @PUT
    @Path("/project")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addProject(Project project) {
        if (project == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            em.persist(project);
        } catch (PersistenceException pex) {
            // Expected unique constraint to fail
            if (pex.getCause() != null && pex.getCause() instanceof ConstraintViolationException) {
                return Response.status(Response.Status.CONFLICT).build();
            } else {
                throw pex;
            }
        }

        return Response.created(UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(project.getId())).build();
    }

    @PUT
    @Path("/occupation")
    @Authorized(UserGroup.MANAGEMENT_EMPLOYEE_ONLY)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addOccupation(Occupation occupation) {
        if (occupation == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            em.persist(occupation);
        } catch (PersistenceException pex) {
            // Expected unique constraint to fail
            return Response.status(Response.Status.CONFLICT).build();
        }

        return Response.created(UriBuilder.fromMethod(OccupationEndpoint.class, "findById").build(occupation.getId())).build();
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
