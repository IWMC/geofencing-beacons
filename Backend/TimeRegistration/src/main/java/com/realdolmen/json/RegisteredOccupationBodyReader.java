package com.realdolmen.json;

import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Occupation;
import com.realdolmen.entity.PersistenceUnit;
import com.realdolmen.entity.RegisteredOccupation;
import com.realdolmen.service.SecurityManager;
import com.realdolmen.validation.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
                String start = root.getString("registeredStart");
                DateTime unconfirmedStart = ISODateTimeFormat.dateTime().parseDateTime(start).toDateTime(DateTimeZone.UTC);
                System.out.println("UTC input string: " + start + " and converted to date in UTC: " + unconfirmedStart);
                DateUtil.enforceUTC(unconfirmedStart);
                ZonedDateTime startDate = unconfirmedStart.toDate().toInstant().atZone(ZoneId.of("Z"));
                ro.setRegisteredStart(new Date(startDate.toInstant().toEpochMilli()));
                System.out.println("Registered start: " + ro.getRegisteredStart());
            }

            if (root.containsKey("registeredEnd")) {
                String end = root.getString("registeredEnd");
                DateTime unconfirmedEnd = ISODateTimeFormat.dateTime().parseDateTime(end).toDateTime(DateTimeZone.UTC);
                DateUtil.enforceUTC(unconfirmedEnd);
                Date endDate = unconfirmedEnd.toDate();
                ro.setRegisteredEnd(endDate);
                System.out.println(ro.getRegisteredEnd() + " - and millis: " + ro.getRegisteredEnd().getTime());
            }

            if(root.containsKey("id")) {
                ro.setId(Long.parseLong(root.get("id").toString()));
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
