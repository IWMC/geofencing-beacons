package com.realdolmen.json;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.realdolmen.entity.Employee;
import com.realdolmen.entity.Project;
import org.hibernate.Hibernate;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.transaction.*;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Provider that converts {@link Employee}s to valid JSON. This will then automatically be used by JAX-RS when returning
 * the appropriate entity in JAX-RS resources.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeWriter implements MessageBodyWriter<Employee> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Employee.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Employee employee, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Employee employee, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try (JsonWriter writer = Json.createWriter(entityStream)) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add("id", employee.getId());
            objectBuilder.add("firstName", employee.getFirstName());
            objectBuilder.add("lastName", employee.getLastName());
            objectBuilder.add("username", employee.getUsername());
            objectBuilder.add("email", employee.getEmail());
            JsonArrayBuilder memberProjectsBuilder = Json.createArrayBuilder();

            employee.getMemberProjects().forEach(p -> {
                JsonObjectBuilder projectBuilder = Json.createObjectBuilder();
                projectBuilder.add("id", p.getId());
                projectBuilder.add("projectNr", p.getProjectNr());
                projectBuilder.add("version", p.getVersion());
                projectBuilder.add("description", p.getDescription());
                projectBuilder.add("name", p.getName());
                projectBuilder.add("startDate", p.getStartDate().getTime());
                projectBuilder.add("endDate", p.getEndDate().getTime());
                memberProjectsBuilder.add(projectBuilder.build());
            });

            objectBuilder.add("memberProjects", memberProjectsBuilder.build());
            writer.write(objectBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
