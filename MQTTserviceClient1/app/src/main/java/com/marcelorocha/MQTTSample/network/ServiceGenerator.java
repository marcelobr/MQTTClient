package com.marcelorocha.MQTTSample.network;

import android.content.Context;
import android.util.Log;

import com.marcelorocha.MQTTSample.BuildConfig;
import com.google.gson.Gson;
import com.marcelorocha.MQTTSample.MQTTClientApp;
import com.marcelorocha.MQTTSample.R;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
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
            .setErrorHandler(new CustomErrorHandler())
            .setLogLevel(BuildConfig.LOGS_ENABLED ?
                    RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);

    /**
     * Create a new instance of a Service Interface.
     * @param serviceClass The Service Interface class.
     * @param <S> The Type of Service Interface class.
     * @return A new instance of passed Service Interface.
     */
    public static <S> S createService(Class<S> serviceClass) {
        final OkHttpClient mClient = new OkHttpClient();
        mClient.setReadTimeout(THIRTY_SECONDS, TimeUnit.MILLISECONDS);
        mClient.setConnectTimeout(THIRTY_SECONDS, TimeUnit.MILLISECONDS);

        builder.setClient(new OkClient(mClient));

        RestAdapter adapter = builder.build();
        return adapter.create(serviceClass);
    }

    /**
     * Converts the complex error structure into a single string you can get with error.getLocalizedMessage() in Retrofit error handlers.
     * Also deals with there being no network available.
     *
     * Uses a few string IDs for user-visible error messages.
     */
    private static class CustomErrorHandler implements ErrorHandler {

        @Override
        public Throwable handleError(RetrofitError cause) {
            Context ctx = MQTTClientApp.getContext();

            String errorDescription;

            if (cause.getKind() == RetrofitError.Kind.NETWORK) {
                errorDescription = ctx.getString(R.string.error_network);
            } else {
                if (cause.getResponse() == null) {
                    errorDescription = ctx.getString(R.string.error_no_response);
                } else {
                    // Error message handling - return a simple error to Retrofit handlers..
                    try {
                        ErrorResponse errorResponse = (ErrorResponse) cause.getBodyAs(ErrorResponse.class);
                        errorDescription = errorResponse.error.data.message;
                    } catch (Exception ex) {
                        try {
                            errorDescription = ctx.getString(R.string.error_network_http_error, cause.getResponse().getStatus());
                        } catch (Exception ex2) {
                            Log.e(getClass().getCanonicalName(), "handleError: " + ex2.getLocalizedMessage());
                            errorDescription = ctx.getString(R.string.error_unknown);
                        }
                    }
                }
            }

            return new Exception(errorDescription);
        }

    }

}
