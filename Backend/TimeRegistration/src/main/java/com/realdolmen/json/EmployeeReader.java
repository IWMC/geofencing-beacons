package com.realdolmen.json;

import com.realdolmen.entity.Employee;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Reader that will parse JSON data and convert it to an {@link Employee}
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeReader implements MessageBodyReader<Employee> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Employee.class.isAssignableFrom(type);
    }

    @Override
    public Employee readFrom(Class<Employee> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        Employee employee = new Employee();
        try (JsonReader reader = Json.createReader(entityStream)) {
            JsonObject object = reader.readObject();
            employee.setEmail(object.getString("email", ""));
            employee.setUsername(object.getString("username", ""));
            employee.setId((long) object.getInt("id", 0));
            employee.setFirstName(object.getString("firstName", ""));
            employee.setLastName(object.getString("lastName", ""));
            employee.setPassword(object.getString("password", ""));
        } catch (JsonException ex) {
            return null;
        }

        return employee;
    }

}
