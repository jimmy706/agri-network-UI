package com.agrinetwork.service;

import android.content.Context;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserService {
    private Context context;
    private OkHttpClient client = new OkHttpClient();

    public UserService(Context context) {
        this.context = context;
    }

    public Call add(User user)  {
        RequestBody body = new FormBody.Builder()
                .add("firstName", user.getFirstName())
                .add("lastName", user.getLastName())
                .add("email", user.getEmail())
                .add("type", user.getType())
                .add("avatar", "")
                .add("phoneNumber", user.getPhoneNumber())
                .add("province", user.getProvince())
                .build();

        Request request = new Request.Builder()
                .url(Variables.SERVICE_DOMAIN + "/users")
                .post(body)
                .build();

        return client.newCall(request);
    }
}
