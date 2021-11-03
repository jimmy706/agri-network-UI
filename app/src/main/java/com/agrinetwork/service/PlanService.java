package com.agrinetwork.service;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.helpers.UrlHelper;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Data;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Call searchPlan(String token, SearchPlanCriteria criteria) {
        UrlHelper<String> helper = new UrlHelper<>();

        Request request = new Request.Builder()
                .url(SERVICE_URL + "?" + helper.convertFromMapToQueryString(criteria.toQuery()))
                .header("Authorization", token)
                .get()
                .build();

        return client.newCall(request);
    }

    public Call getPlanById(String token, String id) {
        Request request = new Request.Builder()
                .url(SERVICE_URL + "/" + id)
                .header("Authorization", token)
                .get()
                .build();

        return client.newCall(request);
    }

    @Data
    public static class SearchPlanCriteria {
        private Boolean expired;
        private String owner;
        private Date from;
        private Date to;

        public SearchPlanCriteria() {

        }

        public SearchPlanCriteria(boolean expired) {
            this.expired = expired;
        }

        public Map<String, String> toQuery() {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
            Map<String, String> result = new HashMap<>();
            if (expired != null) {
                if(expired) {
                    result.put("expired", "1");
                }
                else {
                    result.put("expired", "0");
                }
            }

            if(owner != null) {
                result.put("owner", owner);
            }
            if(from != null) {
                result.put("from", df.format(from));
            }
            if(to != null) {
                result.put("to", df.format(to));
            }

            return result;
        }
    }
}
