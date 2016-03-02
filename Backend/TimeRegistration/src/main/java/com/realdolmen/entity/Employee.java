package com.realdolmen.entity;

import com.realdolmen.entity.validation.New;

import javax.inject.Named;
import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic employee entity. Every user in the system will be an Employee or a subtype of this.
 * The subtypes represent the other actors in the system: {@link ProjectManager} and {@link ManagementEmployee}.
 * Single table inheritance allows JPQL to use polymorphism to query on all subtypes. Since the only additional properties defined
 * in all subtypes is {@link ProjectManager#memberProjects} and this property is a one-to-many property, JPA will
 * add a column to {@link Project}. This means that there will be a major performance advantage compared to
 * a joined inheritance strategy while having no additional overhead except for a discriminator column.
 * <p>
 * This entity will be used to check login credentials of users and will be used to do all operations involving users.
 * For example: assigning a project manager to a project and adding employees as members of a project.
 * <p>
 *     <b>EDIT: </b>We are using the Joined Table inheritance strategy so that the joined table between employees and
 *     projects does not need default values for their foreign keys.
 * </p>
 */
@Entity
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "Employee.findAll", query = "SELECT e FROM Employee e"),
        @NamedQuery(name = "Employee.findByUsername", query = "SELECT e FROM Employee e WHERE e.username = :username"),
        @NamedQuery(name = "Management.findByUsername", query = "SELECT e FROM Employee e WHERE e.username = :username " +
                "AND TYPE(e) IN (ProjectManager, ManagementEmployee)")
})
@Named
@Inheritance(strategy = InheritanceType.JOINED)
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false)
    private Long id;

    @Version
    @Column(name = "version")
    private int version;

    @Column(length = 50)
    @NotNull(message = "firstName.empty")
    @Size(min = 1, max = 50, message = "firstName.length")
    private String firstName;

    @Column(length = 50)
    @NotNull(message = "lastName.empty")
    @Size(min = 1, max = 50, message = "lastName.length")
    private String lastName;

    @Column(length = 15)
    @NotNull(message = "username.empty")
    @Size(min = 1, max = 15, message = "username.length")
    private String username;

    @Column(length = 100)
    @NotNull(message = "email.empty")
    @Pattern(message = "email.pattern", regexp = "[a-z0-9]+[_a-z0-9\\.-]*[a-z0-9]+@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})")
    private String email;

    @Column
    private String hash;

    @Column
    private String salt;

    @Transient
    @NotNull(message = "password.empty", groups = New.class)
    @Size(min = 6, max = 15, message = "password.length", groups = New.class)
    @Pattern(regexp = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]*)?$", message = "password.pattern")
    private String password;

    @ManyToMany
    @JoinTable(
            name = "employee_project",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> memberProjects = new HashSet<>();

    public Employee() {
    }

    public Employee(String password, String salt, String hash, String email, String username, String lastName, String firstName) {
        this.password = password;
        this.salt = salt;
        this.hash = hash;
        this.email = email;
        this.username = username;
        this.lastName = lastName;
        this.firstName = firstName;
    }

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

    public Set<Project> getMemberProjects() {
        return memberProjects;
    }

    public void setMemberProjects(Set<Project> memberProjects) {
        this.memberProjects = memberProjects;
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