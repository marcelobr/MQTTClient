package com.marcelorocha.MQTTSample.network;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * The WebServices API mapping interface.
 */
public interface WebServices {

    @GET("/AllTopics")
    void getAllTopics(Callback<List<String>> callback);

}
