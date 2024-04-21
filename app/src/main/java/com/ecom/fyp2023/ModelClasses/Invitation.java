package com.ecom.fyp2023.ModelClasses;

import java.util.Date;

public class Invitation {

    private String groupId;

    private String userId;

    private Date timestamp;
    private String groupName;

    private String groupDescription;

    private String status;
    private String userName;
    private String userEmail;
    public Invitation() {
    }

    public Invitation(String groupId, String userId, Date timestamp,String groupName,String groupDescription,String status, String userName,String userEmail) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.groupDescription = groupDescription;
        this.status = status;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

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
}
