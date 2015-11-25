package com.marcelorocha.MQTTSample;

import android.app.Application;
import android.content.Context;

import com.marcelorocha.MQTTSample.network.ServiceGenerator;
import com.marcelorocha.MQTTSample.network.WebServices;

/**
 * The Application class of App.
 */
public class MQTTClientApp extends Application {

    /**
     * Represents the instance of WebServices interface.
     */
    private static WebServices webServices;

    /**
     *
     */
    private static MQTTClientApp appInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the Application Instance
        appInstance = this;

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
     * Get the Application context.
     * @return the Application context.
     */
    public static Context getContext() {
        return appInstance.getApplicationContext();
    }

    /**
     * Get the instance of WebServices interface.
     * @return The instance of WebServices interface.
     */
    public static WebServices getWebServices() {
        return webServices;
    }

}
