package com.ecom.fyp2023;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Projects implements Serializable {
    private String title;
    private String description;
    private String priority;
    private String startDate;
    private String endDate;

    private transient String projectId;


    // Default constructor
    public Projects() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Projects(String title, String description, String  priority, String startDate, String endDate) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getter and Setter methods for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter methods for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter methods for priority
    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    // Getter and Setter methods for start date
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // Getter and Setter methods for end date
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Exclude
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
