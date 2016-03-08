package com.realdolmen.timeregistration.model;

public class Occupation {

    private String name = "";

    private int version;

    private String description = "";

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

    private transient final int DTYPE = 1;

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
        if (!(o instanceof Occupation)) return false;

        Occupation that = (Occupation) o;

        if (version != that.version) return false;
        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = version;
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