package com.ecom.fyp2023.ModelClasses;

public class Users {
    private String userName;
    private String userEmail;
    private String fcmToken; // Add this field

    // Default constructor (needed for Firestore deserialization)
    public Users() {
    }

    public Users(String userName, String userEmail, String fcmToken) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.fcmToken = fcmToken;
    }

    // Getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
