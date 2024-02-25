package com.ecom.fyp2023.ModelClasses;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Tasks implements Serializable {
    private String taskName;
    private String taskDetails;
    private String difficulty;
    private String progress;
    private String estimatedTime;

    private List<String> prerequisites;

    private transient String taskId;

    private String completedTime;

    private Date startDate;
    private Date endDate;


    public Tasks() {
        // Default constructor required for Firestore
    }

    public Tasks(String taskName,String taskDetails, String difficulty, String progress, String estimatedTime, List<String> prerequisites,String completedTime,Date startDate,Date endDate) {
        this.taskName = taskName;
        this.taskDetails = taskDetails;
        this.difficulty = difficulty;
        this.progress = progress;
        this.estimatedTime = estimatedTime;
        this.prerequisites = prerequisites;
        this.completedTime = completedTime;
        this.startDate = startDate;
        this.endDate =  endDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getProgress() {
        return progress;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    @Exclude
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(String completedTime) {
        this.completedTime = completedTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
