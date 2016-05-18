package com.realdolmen.timeregistration.service.data;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.realdolmen.timeregistration.model.Project;

import java.sql.SQLException;

public class ProjectDao extends BaseDaoImpl<Project, Integer> {

	protected ProjectDao(Class<Project> dataClass) throws SQLException {
		super(dataClass);
	}

	protected ProjectDao(ConnectionSource connectionSource, Class<Project> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}
}
