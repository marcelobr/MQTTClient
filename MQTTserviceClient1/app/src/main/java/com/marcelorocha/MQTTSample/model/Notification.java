package com.marcelorocha.MQTTSample.model;

/**
 * Model class to represent a Topic.
 */
public class Notification {

    private String dateTime;
    private String message;

    public Notification(String dateTime, String message) {
        this.dateTime = dateTime;
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getMessage() {
        return message;
    }

}
