package com.ecom.fyp2023.ModelClasses;

import java.util.Date;

public class VersionModel {

    private String userId;
    private Date timestamp;
    private String content;
    private String message;

    // Default constructor required for Firestore
    public VersionModel() {
    }

    // Constructor
    public VersionModel(String userId, Date timestamp, String content, String message) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.content = content;
        this.message = message;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
