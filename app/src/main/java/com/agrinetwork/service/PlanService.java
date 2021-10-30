package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PlanService {
    private final Context context;
    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVICE_URL = Variables.SERVICE_DOMAIN + "/plans";

    public PlanService(Context context) {
        this.context = context;
    }

    public Call addPlan(String token, Plan plan) {
        Gson gson = new Gson();
        String jsonPlan = gson.toJson(plan);
        RequestBody body = RequestBody.create(jsonPlan, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(SERVICE_URL)
                .header("Authorization", token)
                .post(body)
                .build();

        return client.newCall(request);
    }

}
