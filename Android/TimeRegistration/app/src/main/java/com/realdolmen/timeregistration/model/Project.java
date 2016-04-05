package com.realdolmen.timeregistration.model;

import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.realdolmen.timeregistration.util.Constants;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Project extends Occupation implements Serializable {

	private DateTime startDate;

	private DateTime endDate;

	private int projectNr;

	private final transient int DTYPE = Constants.PROJECT_DTYPE;

	private Set<Project> subProjects = new HashSet<>();

	private Map<Location, Geofence> geofenceMap = new HashMap<>();

	public Project(String name, String description, int projectNr, DateTime startDate, DateTime endDate) {
		super(name, description);
		this.startDate = startDate;
		this.endDate = endDate;
		this.projectNr = projectNr;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	public int getProjectNr() {
		return projectNr;
	}

	public void setProjectNr(int projectNr) {
		this.projectNr = projectNr;
	}

	public Set<Project> getSubProjects() {
		return subProjects;
	}

	public void setSubProjects(Set<Project> subProjects) {
		this.subProjects = subProjects;
	}

	@Override
	public String toString() {
		String result = getName() + " (#" + projectNr + ")";
		return result;
	}

	public Set<Location> getLocations() {
		return geofenceMap.keySet();
	}

	public Collection<Geofence> getGeofences() {
		return geofenceMap.values();
	}

	public Map<Location, Geofence> getGeofenceMap() {
		return geofenceMap;
	}

	public void setGeofenceMap(Map<Location, Geofence> locations) {
		this.geofenceMap = locations;
	}
}
