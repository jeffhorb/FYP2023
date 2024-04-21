package com.ecom.fyp2023.ModelClasses;

public class Users {
    private String userName;
    private String userEmail;
    private String fcmToken; // Add this field
    private String userId;

    private String role;


    // Default constructor (needed for Firestore deserialization)
    public Users() {


    }

    public Users(String userName, String userEmail, String fcmToken,String userId,String role) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.fcmToken = fcmToken;
        this.userId = userId;
        this.role = role;
    }


    // Getters and setters


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
