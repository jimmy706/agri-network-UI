package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PostItem;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostService {
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVICCE_URL = Variables.SERVICE_DOMAIN + "/posts";

    public PostService(Context context) {
        this.context = context;
    }

    public Call addPost(String token, PostItem postItem) {
        Gson gson = new Gson();
        String json = gson.toJson(postItem);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SERVICCE_URL)
                .header("Authorization", token)
                .post(body)
                .build();

        return client.newCall(request);
    }

}
