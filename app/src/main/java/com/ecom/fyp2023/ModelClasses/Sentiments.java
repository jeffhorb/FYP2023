package com.ecom.fyp2023.ModelClasses;

import java.util.Date;

public class Sentiments {

    private String sentiment;

    private Date timestamp;

    private String groupId;

    private String userAuthId;

    public Sentiments() {

    }

    public Sentiments(String sentiment, Date timestamp,String groupId,String userAuthId) {
        this.sentiment = sentiment;
        this.timestamp = timestamp;
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

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
