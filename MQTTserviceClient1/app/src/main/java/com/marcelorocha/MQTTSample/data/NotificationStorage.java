package com.marcelorocha.MQTTSample.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
        ArrayList<Notification> notifications = new ArrayList<>();

        // Get the App context
        Context context = MQTTClientApp.getContext();

        SharedPreferences storage = context.getSharedPreferences(PREFS_NAME, 0);

        Map<String, String> entries = (Map<String, String>) storage.getAll();

        for (String jsonString : entries.values()) {
            Notification notification = fromJSON(jsonString);

            if (notification != null) {
                // Add to notifications list
                notifications.add(notification);
            } else {
                Log.e(NotificationStorage.class.getSimpleName(),
                        "Could not get Notification from: " + jsonString);
            }
        }

        return notifications;
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

        SharedPreferences storage = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = storage.edit();

        // Generate the persistence key for the Notification
        Map<String, ?> notifications = storage.getAll();
        String strPart = "notification";
        int intPart = 0; int intPartTmp = 0;
        // Try to get the last key generated on Storage
        for (String key : notifications.keySet()) {
            intPartTmp = Integer.parseInt(key.replace(strPart, ""));
            intPart = (intPartTmp > intPart) ? intPartTmp : intPart;
        }

        // Set the Notification to save on Storage
        intPart = intPart + 1;
        editor.putString((strPart + intPart), toJSON(notification));

        // Remove the last notification to keep only the MAX number on Storage
        intPart = intPart - MAX;
        if (intPart > 0) {
            editor.remove((strPart + intPart));
        }

        // Apply the modifications on Storage
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
     * Get the {@link Notification} instance from a passed JSON string.
     * @param jsonString The JSON string to get the {@link Notification}.
     * @return A {@link Notification} instance.
     */
    @Nullable
    private static Notification fromJSON(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String dateTime = jsonObject.getString(JSON_DATETIME_KEY);
            String message = jsonObject.getString(JSON_MESSAGE_KEY);
            // Add to result list
            return new Notification(dateTime, message);
        } catch (JSONException e) {
            return null;
        }
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
