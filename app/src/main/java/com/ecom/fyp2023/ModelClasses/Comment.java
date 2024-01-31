package com.ecom.fyp2023.ModelClasses;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Comment implements Serializable {
    private String Comment;

    private Timestamp timestamp;

    // Default constructor
    public Comment() {
        // Default constructor is needed for Firebase or other data binding frameworks
    }

    // Parameterized constructor
    public Comment(String comment, Timestamp timeStamp) {
        this.Comment = comment;
        this.timestamp = timeStamp;

    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }
}
