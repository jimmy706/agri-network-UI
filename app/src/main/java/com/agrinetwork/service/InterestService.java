package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Interest;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class InterestService {

    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/interests";

    private final OkHttpClient client = new OkHttpClient();
    private final Context context;

    public InterestService(Context context) {
        this.context = context;
    }

    public Call addNew(String token, Interest interest) {
        Gson gson = new Gson();
        String jsonInterest = gson.toJson(interest);
        RequestBody body = RequestBody.create(jsonInterest, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(SERVICE_URL)
                .post(body)
                .build();

        return client.newCall(request);
    }

}
