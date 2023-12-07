package com.ecom.fyp2023;

public class Users {

    // variables for storing our data.
    private String userName, userEmail;

    public Users() {
        // empty constructor
        // required for Firebase.
    }

    // Constructor for all variables.
    public Users(String userName, String userEmail) {
        this.userName = userName;
        this.userEmail = userEmail;
    }

    // getter methods for all variables.
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    // setter method for all variables.
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }



}