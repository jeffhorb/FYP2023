package com.ecom.fyp2023.ModelClasses;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Projects implements Serializable {
    private String title;
    private String description;
    private String progress;
    private String priority;
    private String startDate;
    private String endDate;

    private transient String projectId;


    // Default constructor
    public Projects() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Projects(String title, String description, String  priority, String startDate, String endDate,String progress) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
    }

    // Getter and Setter methods for title
    public String getTitle() {
        return title;
    }

    // Getter and Setter methods for description
    public String getDescription() {
        return description;
    }

    // Getter and Setter methods for priority
    public String getPriority() {
        return priority;
    }

    // Getter and Setter methods for start date
    public String getStartDate() {
        return startDate;
    }

    // Getter and Setter methods for end date
    public String getEndDate() {
        return endDate;
    }

    @Exclude
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProgress() {
        return progress;
    }

}
