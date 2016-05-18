package com.realdolmen.timeregistration.util.json;

import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.model.Occupation;
import com.realdolmen.timeregistration.model.Project;
import com.realdolmen.timeregistration.model.Task;
import com.realdolmen.timeregistration.service.location.geofence.GeofenceUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OccupationDeserializer implements JsonDeserializer<Occupation> {
	@Override
	public Occupation deserialize(JsonElement _json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Occupation out = null;
		JsonObject json = _json.getAsJsonObject();
		if (json.has("DTYPE")) {
			int DTYPE = json.get("DTYPE").getAsInt();
			switch (DTYPE) {
				case RC.dtypes.PROJECT_DTYPE:
					return createProject(json, context);
				case RC.dtypes.TASK_DTYPE:
					return createTask(json, context);
				case RC.dtypes.OCCUPATION_DTYPE:
				default:
					return createOccupation(json, context);
			}
		}

		return createOccupation(json, context);
	}

	private Task createTask(JsonObject json, JsonDeserializationContext context) {
		return new Gson().fromJson(json, Task.class);
	}

	private Occupation createOccupation(JsonObject json, JsonDeserializationContext context) {
		return new Gson().fromJson(json, Occupation.class);
	}

	private Project createProject(JsonObject json, JsonDeserializationContext context) {
		String name = json.get("name").getAsString();
		int version = json.get("version").getAsInt();
		String description = json.get("description").getAsString();
		long id = json.get("id").getAsLong();
		String startDate = json.get("startDate").getAsString();
		String endDate = json.get("endDate").getAsString();
		int projectNr = json.get("projectNr").getAsInt();
		Project[] subProjects = context.deserialize(json.get("subProjects"), Project[].class);
		Set<Location> locations = new HashSet<>();
		double estimatedHours = 0;
		if (json.has("locations"))
			for (JsonElement jsonElement : json.get("locations").getAsJsonArray()) {
				JsonObject loc = jsonElement.getAsJsonObject();
				Location l = new Location("");
				l.setLatitude(loc.get("lat").getAsDouble());
				l.setLongitude(loc.get("long").getAsDouble());
				locations.add(l);
			}

		if(json.has("estimatedHours")) {
			estimatedHours = json.get("estimatedHours").getAsDouble();
		}
		Project p = new Project(name, description, projectNr, DateTime.parse(startDate).toDateTime(DateTimeZone.UTC), DateTime.parse(endDate).toDateTime(DateTimeZone.UTC));
		if (subProjects != null)
			p.setSubProjects(new HashSet<>(Arrays.asList(subProjects)));
		p.setId(id);
		Map<Location, Geofence> map = new HashMap<>();
		for (Location location : locations) {
			map.put(location, GeofenceUtils.createGeofence(id, location, 1000));
		}
		p.setGeofenceMap(map);
		p.setEstimatedHours(estimatedHours);
		return p;
	}
}
