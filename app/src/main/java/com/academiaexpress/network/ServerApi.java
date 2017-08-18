package com.academiaexpress.network;

import android.content.Context;

import com.academiaexpress.BuildConfig;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerApi {

    private static ServerApi api;
    private static ApiEndpoints apiService;
    private static Retrofit retrofit;
    private final static HttpLoggingInterceptor.Level LOG_LEVEL = BuildConfig.DEBUG
            ? HttpLoggingInterceptor.Level.BODY
            : HttpLoggingInterceptor.Level.BASIC;


    private void initRetrofit(final Context context) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(LOG_LEVEL);
        OkHttpClient client = new OkHttpClient
                .Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .addInterceptor(interceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(ServerConfig.INSTANCE.getApiURL())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiEndpoints.class);
    }

    public static ServerApi get(Context context) {
        if (api == null) {
            synchronized (ServerApi.class) {
                if (api == null) {
                    api = new ServerApi(context);
                }
            }
        }
        return api;
    }

    public static Retrofit retrofit() {
        return retrofit;
    }

    private ServerApi(Context context) {
        initRetrofit(context);
    }

    public ApiEndpoints api() {
        return apiService;
    }
}
