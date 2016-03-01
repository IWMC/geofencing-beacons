package com.realdolmen.timeregistration.model;

/**
 * Created by Brent on 29/02/2016.
 */
public class Session {

    private String username;
    private String password;

    private String jwtToken;

    public Session(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
