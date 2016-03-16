package com.realdolmen.json;

import javax.json.JsonObject;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An object representing a password credentials object when updating the employee password in
 * {@link com.realdolmen.rest.EmployeeEndpoint#updatePassword(Long, JsonObject)}. It acts as a wrapper of data with the
 * necessary bean validation and XML annotations to allow for extensible JSON parsing and validation.
 */
@XmlRootElement
public class EmployeePasswordCredentials {

    @NotNull(message = "password.empty")
    @Size(min = 6, max = 15, message = "password.length")
    @Pattern(regexp = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]*)?$", message = "password.pattern")
    private String password;

    @NotNull(message = "passwordRepeat.empty")
    private String passwordRepeat;

    @NotNull(message = "id.empty")
    @Min(value = 1, message = "id.invalid")
    private Long id;

    public EmployeePasswordCredentials() {
    }

    public EmployeePasswordCredentials(String password, String passwordRepeat, Long id) {
        this.password = password;
        this.passwordRepeat = passwordRepeat;
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
