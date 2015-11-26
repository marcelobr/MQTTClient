package com.marcelorocha.MQTTSample.network;

import com.marcelorocha.MQTTSample.model.Topic;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * The WebServices API mapping interface.
 */
public interface WebServices {

    @GET("/GetTopics")
    void getTopics(@Query("macaddress") String macaddress, Callback<List<Topic>> callback);

    @GET("/SendSubscribe")
    void sendSubscribe(@Query("macaddress") String macaddress,
                       @Query("topic") String topic,
                       Callback<Void> callback);

    @GET("/SendUnsubscribe")
    void sendUnSubscribe(@Query("macaddress") String macaddress,
                         @Query("topic") String topic,
                         Callback<Void> callback);

}
