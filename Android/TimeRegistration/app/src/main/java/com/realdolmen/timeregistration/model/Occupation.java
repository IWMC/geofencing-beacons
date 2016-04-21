package com.realdolmen.timeregistration.model;

import com.realdolmen.timeregistration.util.Constants;

import java.io.Serializable;

public class Occupation implements Serializable {

	private String name = "";

	private int version;

	private String description = "";

	public Occupation(String name, String description) {
		this(name);
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private transient final int DTYPE = Constants.OCCUPATION_DTYPE;

	public Occupation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (name != null && !name.trim().isEmpty())
			result += "name: " + name;
		if (description != null && !description.trim().isEmpty())
			result += ", description: " + description;
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Occupation that = (Occupation) o;

		if (version != that.version) return false;
		if (id != that.id) return false;
		if (!name.equals(that.name)) return false;
		return !(description != null ? !description.equals(that.description) : that.description != null);

	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + version;
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (int) (id ^ (id >>> 32));
		return result;
	}

	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}