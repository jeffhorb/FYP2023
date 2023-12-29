package com.ecom.fyp2023;

public class Tasks {

    private String taskDetails;
    private String difficulty;
    private String progress;
    private String estimatedTime;

    // Constructors, getters, and setters

    public Tasks() {
        // Default constructor required for Firestore
    }

    public Tasks( String taskDetails, String difficulty, String progress, String estimatedTime) {

        this.taskDetails = taskDetails;
        this.difficulty = difficulty;
        this.progress = progress;
        this.estimatedTime = estimatedTime;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}

