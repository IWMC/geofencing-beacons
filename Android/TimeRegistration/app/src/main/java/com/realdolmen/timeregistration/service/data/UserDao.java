package com.realdolmen.timeregistration.service.data;

import android.support.annotation.Nullable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.realdolmen.timeregistration.model.User;

import java.sql.SQLException;

public class UserDao extends BaseDaoImpl<User, Integer> {


	public UserDao(ConnectionSource connectionSource) throws SQLException {
		this(connectionSource, User.class);
	}

	public UserDao(ConnectionSource connectionSource, Class<User> dataClass) throws SQLException {
		super(connectionSource, dataClass);
	}
}
