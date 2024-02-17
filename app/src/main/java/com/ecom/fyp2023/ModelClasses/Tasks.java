package com.ecom.fyp2023.ModelClasses;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.List;

public class Tasks implements Serializable {

    private String taskDetails;
    private String difficulty;
    private String progress;
    private String estimatedTime;

    // New field for prerequisites
    private List<String> prerequisites;

    private transient String taskId;

    // Constructors, getters, and setters

    public Tasks() {
        // Default constructor required for Firestore
    }

    public Tasks(String taskDetails, String difficulty, String progress, String estimatedTime, List<String> prerequisites) {
        this.taskDetails = taskDetails;
        this.difficulty = difficulty;
        this.progress = progress;
        this.estimatedTime = estimatedTime;
        this.prerequisites = prerequisites;
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
}
