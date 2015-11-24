package com.example.MQTT;

import android.app.Application;

import com.example.MQTT.network.ServiceGenerator;
import com.example.MQTT.network.WebServices;

import retrofit.RestAdapter;

/**
 * The Application class of App.
 */
public class MQTTClientApp extends Application {

    /**
     * Represents the instance of WebServices interface.
     */
    private static WebServices webServices;

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform Services initialization
        initServices();
    }

    /**
     * Initialize App's Services.
     */
    private void initServices() {
        // Create an instance of WebServices.
        webServices = ServiceGenerator.createService(WebServices.class);
    }

    /**
     * Get the instance of WebServices interface.
     * @return The instance of WebServices interface.
     */
    public static WebServices getWebServices() {
        return webServices;
    }

}
