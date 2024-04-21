package com.ecom.fyp2023.ModelClasses;

public class Notes {
    private String note;

    String groupId;

    String userAuthId;

    public Notes(){

    }

    public Notes(String note, String groupId,String userAuthId) {
        this.note = note;
        this.groupId = groupId;
        this.userAuthId = userAuthId;

    }

    public String getUserAuthId() {
        return userAuthId;
    }

    public void setUserAuthId(String userAuthId) {
        this.userAuthId = userAuthId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Notes(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
