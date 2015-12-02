package com.marcelorocha.MQTTSample.network;

/**
 * A model class to represents Web Services errors.
 *
 * On error the server should sends JSON:
 * { "error": { "data": { "message":"A thing went wrong" } } }
 */
public class ErrorResponse {
    Error error;

    public static class Error {
        Data data;

        public static class Data {
            String message;
        }
    }

}
