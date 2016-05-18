package com.realdolmen.timeregistration.service.data;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.realdolmen.timeregistration.model.Occupation;

import java.sql.SQLException;

public class OccupationDao extends BaseDaoImpl<Occupation, Integer> {
	protected OccupationDao(Class<Occupation> dataClass) throws SQLException {
		super(dataClass);
	}

	protected OccupationDao(ConnectionSource connectionSource, Class<Occupation> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}
}
