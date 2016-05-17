package com.realdolmen.timeregistration.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.realdolmen.timeregistration.service.data.UserDao;

@DatabaseTable(tableName = "RDUser", daoClass = UserDao.class)
public class User {

	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField
	String username;

	@DatabaseField
	String token;

	public User(String username, String token) {
		this.username = username;
		this.token = token;
	}

	public User() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", username='" + username + '\'' +
				", token='" + token + '\'' +
				'}';
	}
}
