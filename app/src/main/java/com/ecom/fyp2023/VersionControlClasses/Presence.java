package com.ecom.fyp2023.VersionControlClasses;

public class Presence {
    private String authId;
    private String username;

    // Constructor, getters, and setters
    public Presence() {
    }

    public Presence(String authId, String username) {
        this.authId = authId;
        this.username = username;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
