package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ProvinceService {
    private OkHttpClient client = new OkHttpClient();
    private Context context;

    public ProvinceService(Context context) {
        this.context = context;
    }

    public Call getAll() {
        Request request = new Request.Builder()
                .url(Variables.SERVICE_DOMAIN + "/provinces")
                .get()
                .build();

        return client.newCall(request);
    }
}
