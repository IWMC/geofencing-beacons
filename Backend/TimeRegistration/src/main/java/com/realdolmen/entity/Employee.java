package com.realdolmen.entity;

import javax.persistence.*;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@NamedQueries({
		@NamedQuery(name = "Employee.findAll", query = "SELECT DISTINCT e FROM Employee e ORDER BY e.id")
})
public class Employee implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;
	@Version
	@Column(name = "version")
	private int version;

	@Column(length = 50)
	private String firstName;

	@Column(length = 50)
	private String lastName;

	@Column(length = 15)
	private String username;

	@Column(length = 100)
	private String email;

	@Column
	private String hash;

	@Column
	private String salt;

	@Transient
	private String password;

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Employee)) {
			return false;
		}
		Employee other = (Employee) obj;
		if (id != null) {
			if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		String result = getClass().getSimpleName() + " ";
		if (firstName != null && !firstName.trim().isEmpty())
			result += "firstName: " + firstName;
		if (lastName != null && !lastName.trim().isEmpty())
			result += ", lastName: " + lastName;
		if (username != null && !username.trim().isEmpty())
			result += ", username: " + username;
		if (email != null && !email.trim().isEmpty())
			result += ", email: " + email;
		if (hash != null && !hash.trim().isEmpty())
			result += ", hash: " + hash;
		if (salt != null && !salt.trim().isEmpty())
			result += ", salt: " + salt;
		return result;
	}
}