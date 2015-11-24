package com.example.MQTT.network;

import java.util.List;

import retrofit.http.GET;

/**
 * The WebServices API mapping interface.
 */
public interface WebServices {

    @GET("/AllTopics")
    List<String> getAllTopics();

}
