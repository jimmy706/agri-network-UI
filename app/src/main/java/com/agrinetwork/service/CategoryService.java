package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CategoryService {
    private OkHttpClient client = new OkHttpClient();
    private final Context context;

    public CategoryService(Context context){
        this.context = context;
    }

    public Call getCategory(String token){
        Request request = new Request.Builder()
                .header("Authorization", token)
                .url(Variables.SERVICE_DOMAIN + "/category")
                .get()
                .build();

        return client.newCall(request);
    }
}
