package com.agrinetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.agrinetwork.components.UserAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Response;

public class FriendsActivity extends AppCompatActivity {
    private final List<User> friends = new ArrayList<>();
    private final Gson gson = new Gson();

    private String token;
    private UserService userService;
    private UserAdapter userAdapter;

    private RecyclerView friendsRecyclerView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        userService = new UserService(this);

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");

        Intent intent = getIntent();
        String userId = intent.getExtras().getString("userId");

        toolbar = findViewById(R.id.toolbar);

        userAdapter = new UserAdapter(friends, this, true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        friendsRecyclerView = findViewById(R.id.friend_list);
        friendsRecyclerView.setLayoutManager(linearLayoutManager);
        friendsRecyclerView.setAdapter(userAdapter);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        fetchUserFriends(userId);
    }

    private void fetchUserFriends(String userId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<User>> future = executorService.submit(() -> {
            Call call = userService.getFriends(token, userId);
            List<User> responseData = new ArrayList<>();
            Response response = call.execute();
            if(response.code() == 200) {
                Type userListType = new TypeToken<List<User>>(){}.getType();
                 responseData.addAll(gson.fromJson(response.body().string(), userListType));
            }
            return responseData;
        });

        try {
            friends.addAll(future.get());
            renderData();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void renderData() {
        if(!friends.isEmpty()) {
            toolbar.setTitle(getText(R.string.friended) + ": " + friends.size());
            userAdapter.notifyDataSetChanged();
        }
    }
}