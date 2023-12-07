package com.ecom.fyp2023;

public class Tasks {

    private String taskDetails;
    private String difficulty;
    private int progress;
    private int estimatedTime;

    // Constructors, getters, and setters

    public Tasks() {
        // Default constructor required for Firestore
    }

    public Tasks( String taskDetails, String difficulty, int progress, int estimatedTime) {

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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}

