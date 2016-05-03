package com.realdolmen.timeregistration.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.realdolmen.timeregistration.RC;

import java.io.Serializable;

@DatabaseTable(tableName = "Occupation")
public class Occupation implements Serializable {

	private transient final int DTYPE = RC.dtypes.OCCUPATION_DTYPE;
	private int version;

	@DatabaseField
	private String name = "";

	@DatabaseField
	private String description = "";

	@DatabaseField(id = true)
	private long id;

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
}