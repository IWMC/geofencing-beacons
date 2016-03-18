package com.realdolmen.json;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

@Produces(MediaType.APPLICATION_JSON)
@Provider
public class RegisteredOccupationBodyReader implements MessageBodyReader<RegisteredOccupation> {

    @Inject
    private SecurityManager sm;

    @PersistenceContext(unitName = PersistenceUnit.PRODUCTION)
    private EntityManager em;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RegisteredOccupation.class.isAssignableFrom(type);
    }

    @Override
    public RegisteredOccupation readFrom(Class<RegisteredOccupation> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

        RegisteredOccupation ro = new RegisteredOccupation();

        try (JsonReader reader = Json.createReader(entityStream)) {
            JsonObject root = reader.readObject();
            Employee existingEmployee = sm.findEmployee();
            ro.setRegistrar(existingEmployee);
            if (root.containsKey("registeredStart")) {
                long start = Long.parseLong(root.get("registeredStart").toString());
                Date startDate = new DateTime(start, DateTimeZone.UTC).toDate();
                ro.setRegisteredStart(startDate);
            }

            if (root.containsKey("registeredEnd")) {
                long end = Long.parseLong(root.get("registeredEnd").toString());
                Date endDate = new DateTime(end, DateTimeZone.UTC).toDate();
                ro.setRegisteredEnd(endDate);
            }

            if(root.containsKey("occupation")) {
                JsonObject occ = root.getJsonObject("occupation");
                if(occ.containsKey("id")) {
                    long id = Long.parseLong(occ.get("id").toString());
                    Occupation o = em.find(Occupation.class, id);
                    ro.setOccupation(o);
                }
            }
        }

        return ro;
    }
}
