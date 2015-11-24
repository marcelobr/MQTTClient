package com.example.MQTT.network;

import com.example.MQTT.BuildConfig;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Utility class to create Retrofit service instances.
 */
public class ServiceGenerator {

    private static final int THIRTY_SECONDS = 30 * 1000;

    /**
     * Define RestAdapter with default configs.
     */
    private static RestAdapter.Builder builder = new RestAdapter.Builder()
            .setEndpoint(BuildConfig.API_URL)
            .setConverter(new GsonConverter(new Gson()))
            .setLogLevel(BuildConfig.LOGS_ENABLED ?
                    RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);

    public static <S> S createService(Class<S> serviceClass) {
        final OkHttpClient mClient = new OkHttpClient();
        mClient.setReadTimeout(THIRTY_SECONDS, TimeUnit.MILLISECONDS);
        mClient.setConnectTimeout(THIRTY_SECONDS, TimeUnit.MILLISECONDS);

        builder.setClient(new OkClient(mClient));

        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }

}
