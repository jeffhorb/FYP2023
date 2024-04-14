package com.ecom.fyp2023.MiroWhiteBoardIntegration;

import com.google.gson.annotations.SerializedName;

public class Whiteboard {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("viewLink")
    private String viewLink;

    // Constructor, getters, and setters
    public Whiteboard(String id, String name, String viewLink) {
        this.id = id;
        this.name = name;
        this.viewLink = viewLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getViewLink() {
        return viewLink;
    }

    public void setViewLink(String viewLink) {
        this.viewLink = viewLink;
    }
}
