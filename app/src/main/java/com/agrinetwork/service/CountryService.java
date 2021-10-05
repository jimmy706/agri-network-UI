package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CountryService {
    private static final String SERVICE_URL = Variables.COUNTRY_SERVICE_DOMAIN;
    private final OkHttpClient client;
    private final Context context;

    public CountryService(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    public Call getProvinceDetail(int code) {
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/p/" + code + "?depth=2")
                .get()
                .build();

        return client.newCall(request);
    }

    public Call getDistrictDetail(int code) {
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/d/" + code + "?depth=2")
                .get()
                .build();

        return client.newCall(request);
    }

}
