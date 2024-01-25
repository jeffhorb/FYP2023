package com.ecom.fyp2023.ModelClasses;

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

    public String getDifficulty() {
        return difficulty;
    }

    public String getProgress() {
        return progress;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }

}

