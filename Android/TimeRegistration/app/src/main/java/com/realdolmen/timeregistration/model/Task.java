package com.realdolmen.timeregistration.model;

public class Task extends Occupation {

	private double estimatedHours;

	private Project project;

	private long projectId;

	public Task() {
	}

	public Task(String name, String description, double estimatedHours, Project project) {
		super(name, description);
		this.estimatedHours = estimatedHours;
		this.project = project;
	}

	public double getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(double estimatedHours) {
		this.estimatedHours = estimatedHours;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}
}