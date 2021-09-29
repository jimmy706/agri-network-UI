package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TagService {
    private OkHttpClient client = new OkHttpClient();
    private final Context context;

    public TagService(Context context) {
        this.context = context;
    }

    public Call getPostTag() {
        Request request = new Request.Builder()
                .url(Variables.SERVICE_DOMAIN + "/tag/postTag")
                .get()
                .build();

        return client.newCall(request);
    }
}
