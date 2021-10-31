package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class RecommendService {
    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/recommend";
    private final OkHttpClient client = new OkHttpClient();
    private final Context context;

    public RecommendService(Context context) {
        this.context = context;
    }

    public Call getRecommendedUsers(String token) {
        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL + "/users")
                .build();

        return client.newCall(request);
    }

    public Call getProductsFeed(String token) {
        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL + "/products")
                .build();

        return client.newCall(request);
    }

    public Call getUserDemand(String token) {
        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL + "/users/on-demand")
                .build();

        return client.newCall(request);
    }

}
