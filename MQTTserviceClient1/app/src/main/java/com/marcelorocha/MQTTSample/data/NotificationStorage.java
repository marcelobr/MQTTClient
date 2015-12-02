package com.marcelorocha.MQTTSample.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.marcelorocha.MQTTSample.MQTTClientApp;
import com.marcelorocha.MQTTSample.model.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class to handle the Notification storage.
 */
public class NotificationStorage {

    // The JSON keys to use on Storage content.
    private static final String JSON_DATETIME_KEY = "dateTime";
    private static final String JSON_MESSAGE_KEY = "message";

    /**
     * Represents the name of Notifications storage.
     */
    private static final String PREFS_NAME = "notifications";

    /**
     * Represents the max number of Notification to store.
     */
    private static final int MAX = 10;

    /**
     * Get the list of {@link Notification} of App.
     * @return The list of {@link Notification} of App.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static List<Notification> getAllNotifications() {
        ArrayList<Notification> result = new ArrayList<>();

        // Get the App context
        Context context = MQTTClientApp.getContext();

        SharedPreferences notifications = context.getSharedPreferences(PREFS_NAME, 0);

        Map<String, String> entries = (Map<String, String>) notifications.getAll();

        for (String notification : entries.values()) {
            try {
                JSONObject jsonObject = new JSONObject(notification);
                String dateTime = jsonObject.getString(JSON_DATETIME_KEY);
                String message = jsonObject.getString(JSON_MESSAGE_KEY);

                // Add to result list
                result.add(new Notification(dateTime, message));
            } catch (JSONException e) {
                Log.e(NotificationStorage.class.getSimpleName(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * Save a notification on Storage.
     * @param message The message of notification.
     */
    public static void saveNotification(String message) {
        // Get the App context
        Context context = MQTTClientApp.getContext();

        // Create a Notification to save
        Notification notification = new Notification(getCurrentTimeStamp(), message);

        SharedPreferences notifications = context.getSharedPreferences(PREFS_NAME, 0);

        // Generate the persistence key for the Notification
        final int total = notifications.getAll().size();
        String key = "notification" + (total % MAX);

        // Save the Notification
        SharedPreferences.Editor editor = notifications.edit();
        editor.putString(key, toJSON(notification));
        editor.apply();
    }

    /**
     * Helper method for get the current system date on format "yyyy-MM-ddTHH:mm:ss".
     * @return The current system date on format "yyyy-MM-ddTHH:mm:ss".
     */
    private static String getCurrentTimeStamp() {
        return getDateFormat().format(new Date());
    }

    /**
     * Get the {@link DateFormat} used on Storage.
     * @return The {@link DateFormat} of Storage.
     */
    public static DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    }

    /**
     * Transform a {@link Notification} to JSON String.
     * @param notification The {@link Notification} instance to transform.
     * @return A JSON String from {@link Notification} instance object.
     */
    private static String toJSON(Notification notification) {
        return "{" + JSON_DATETIME_KEY + "='" + notification.getDateTime() + "\'" +
                ", " + JSON_MESSAGE_KEY + "='" + notification.getMessage() + "\'}";
    }

}
