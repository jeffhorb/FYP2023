package com.ecom.fyp2023.ModelClasses;


import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

public class Projects implements Serializable {
    private String title;
    private String description;
    private String progress;
    private String priority;
    private String startDate;
    private String endDate;
    private Date actualEndDate;

    private String groupId;

    private String userAuthId;

    private transient String projectId;


    // Default constructor
    public Projects() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Projects(String title, String description, String  priority, String startDate, String endDate,String progress, Date timestamp,String groupId,String userAuthId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.actualEndDate = timestamp;
        this.groupId = groupId;
        this.userAuthId = userAuthId;
    }

    public String getUserAuthId() {
        return userAuthId;
    }

    public void setUserAuthId(String userAuthId) {
        this.userAuthId = userAuthId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }
}
