package com.sandeep.vaanibridge;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * VaaniBridge Central Network Client
 * Configured with high timeouts (60s) for cloud cold starts and logging for diagnostics.
 */
public class NetworkClient {
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;

    public static synchronized OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }

    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL + "/") // Ensuring trailing slash for Retrofit
                    .client(getOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /**
     * Helper to return the OkHttpClient instance for direct use (e.g., in MainActivity).
     */
    public static OkHttpClient getInstance() {
        return getOkHttpClient();
    }
}
