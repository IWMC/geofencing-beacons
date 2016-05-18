package com.realdolmen.timeregistration.model;

import com.realdolmen.timeregistration.service.repository.Repositories;

public class Task extends Occupation {

	private Project project;

	private long projectId;

	public Task() {
	}

	public Task(String name, String description, double estimatedHours, Project project) {
		super(name, description);
		setEstimatedHours(estimatedHours);
		this.project = project;
	}

	public Project getProject() {
		if(project == null) {
			project = (Project) Repositories.occupationRepository().getById(getProjectId());
		}
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