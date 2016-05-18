package com.realdolmen.timeregistration.model;

import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.realdolmen.timeregistration.RC;
import com.realdolmen.timeregistration.service.data.ProjectDao;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@DatabaseTable(daoClass = ProjectDao.class)
public class Project extends Occupation implements Serializable {

	@DatabaseField
	private DateTime startDate;

	@DatabaseField
	private DateTime endDate;

	@DatabaseField
	private int projectNr;

	@DatabaseField(foreign = true)
	private Project parent;

	private final transient int DTYPE = RC.dtypes.PROJECT_DTYPE;

	@ForeignCollectionField // TODO: 16/05/2016 Check if sets work in the database
	private Collection<Project> subProjects = new HashSet<>();

	private transient Map<Location, Geofence> geofenceMap = new HashMap<>();

	public Project() {
	}

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

	public Collection<Project> getSubProjects() {
		return subProjects;
	}

	public void setSubProjects(Set<Project> subProjects) {
		this.subProjects = subProjects;
	}

	@Override
	public String toString() {
		return getName();
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
