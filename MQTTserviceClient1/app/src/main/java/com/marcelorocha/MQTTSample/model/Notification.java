package com.marcelorocha.MQTTSample.model;

/**
 * Model class to represent a Topic.
 */
public class Notification implements Comparable<Notification> {

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

    @Override
    public int compareTo(Notification o) {
        return this.getDateTime().compareTo(o.getDateTime());
    }

}
