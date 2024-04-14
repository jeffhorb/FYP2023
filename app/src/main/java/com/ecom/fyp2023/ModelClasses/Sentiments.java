package com.ecom.fyp2023.ModelClasses;

import java.util.Date;

public class Sentiments {

    private String sentiment;

    private Date timestamp;

    public Sentiments() {

    }

    public Sentiments(String sentiment, Date timestamp) {
        this.sentiment = sentiment;
        this.timestamp = timestamp;

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
