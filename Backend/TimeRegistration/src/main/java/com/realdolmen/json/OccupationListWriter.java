package com.realdolmen.json;

import com.realdolmen.entity.RegisteredOccupation;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by BCCAZ45 on 7/03/2016.
 */
public class OccupationListWriter implements MessageBodyWriter<RegisteredOccupation> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RegisteredOccupation.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(RegisteredOccupation registeredOccupation, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(RegisteredOccupation registeredOccupation, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try (JsonWriter writer = Json.createWriter(entityStream)) {
            JsonObjectBuilder test = Json.createObjectBuilder().add("startTime", registeredOccupation.getRegisteredStart().getTime());
            writer.write(test.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
