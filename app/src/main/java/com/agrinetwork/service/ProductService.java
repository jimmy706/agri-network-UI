package com.agrinetwork.service;

import android.content.Context;
<<<<<<< HEAD

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
=======
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.agrinetwork.config.Variables;
import com.agrinetwork.helpers.UrlHelper;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ProductService {
    private final Context context;
    private final OkHttpClient client;
    private static final String DOMAIN = Variables.SERVICE_DOMAIN + "/products";

    public ProductService(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Call searchProducts(String token, SearchProductCriteria criteria) {
        UrlHelper<String> urlHelper = new UrlHelper<>();
        Map<String, String> queries = criteria.toQuery();

        Request request = new Request.Builder()
                .get()
                .header("Authorization", token)
                .url(DOMAIN + "?" + urlHelper.convertFromMapToQueryString(queries))
>>>>>>> 3643f2ea19bfae5f78b0f26eae71f9622599db51
                .build();

        return client.newCall(request);
    }
<<<<<<< HEAD
=======

    @Data
    public static class SearchProductCriteria {
        private String name;
        private String owner;
        private double priceFrom;
        private double priceTo;
        private int page;
        private int limit;

        public SearchProductCriteria() {
            this.page = 1;
            this.limit = 9;
        }

        public SearchProductCriteria(int page, int limit) {
            this.page = page;
            this.limit = limit;
        }

        public Map<String, String> toQuery() {
            Map<String, String> result = new HashMap<>();
            result.put("limit", Integer.toString(limit));
            result.put("page", Integer.toString(page));

            if(name != null && !name.trim().isEmpty()) {
                result.put("name", name);
            }
            if(owner != null && !owner.trim().isEmpty()) {
                result.put("owner", owner);
            }
            if(priceFrom > 0 && priceTo > priceFrom) {
                result.put("priceFrom", Double.toString(priceFrom));
                result.put("priceTo", Double.toString(priceTo));
            }

            return result;
        }
    }
>>>>>>> 3643f2ea19bfae5f78b0f26eae71f9622599db51
}
