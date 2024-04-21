package com.ecom.fyp2023.ModelClasses;

import java.util.Date;

public class VersionModel {

    private String userId;
    private Date timestamp;
    private String content;
    private String message;
    private String groupId;

    // Default constructor required for Firestore
    public VersionModel() {
    }

    // Constructor
    public VersionModel(String userId, Date timestamp, String content, String message,String groupId) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.content = content;
        this.message = message;
        this.groupId = groupId;
    }

    // Getters and setters


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

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
