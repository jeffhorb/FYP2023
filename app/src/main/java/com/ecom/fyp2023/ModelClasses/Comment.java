package com.ecom.fyp2023.ModelClasses;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String Comment;

    private Date timestamp;


    private String currentUserId;

    private String userEmail;
    private  String userName;

    private String groupId;

    String fileUrl;

    // Default constructor
    public Comment() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Comment(String comment, Date timeStamp,String currentUserId,String userEmail,String userName,String groupId,String fileUrl) {
        this.Comment = comment;
        this.timestamp = timeStamp;
        this.currentUserId = currentUserId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.groupId = groupId;
        this.fileUrl = fileUrl;

    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
