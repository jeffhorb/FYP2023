package com.ecom.fyp2023.ModelClasses;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private String Comment;

    private Date timestamp;


    private String currentUserId;

    private String userEmail;
    private  String userName;

    // Default constructor
    public Comment() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Comment(String comment, Date timeStamp,String currentUserId,String userEmail,String userName) {
        this.Comment = comment;
        this.timestamp = timeStamp;
        this.currentUserId = currentUserId;
        this.userEmail = userEmail;
        this.userName = userName;

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
