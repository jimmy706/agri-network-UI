package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.Product;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProductService {
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/products";

    public ProductService(Context context) {
        this.context = context;
    }

    public Call addProduct(String token, Product productItem) {
        Gson gson = new Gson();
        String json = gson.toJson(productItem);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SERVICE_URL)
                .header("Authorization", token)
                .post(body)
                .build();

        return client.newCall(request);
    }
}
