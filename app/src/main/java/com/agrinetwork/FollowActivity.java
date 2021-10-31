package com.agrinetwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.components.UserAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FollowActivity extends AppCompatActivity {
    private final List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private UserService userService;
    private String token;

    private String userId;
    private SwipeRefreshLayout refreshLayout;
    private final Gson gson = new Gson();

    private final String  FOLLOWINGS = "FOLLOWINGS";
    private final String  FOLLOWERS = "FOLLOWERS";
    private String type;
    private int totalFollowing;
    private int totalFollower;
    private MaterialToolbar toolbar;
    private TextView noResultFoundMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");



        userService = new UserService(this);
        userAdapter = new UserAdapter(users, this);

        Intent intent = getIntent();
        type = intent.getExtras().getString("type");
        userId = intent.getExtras().getString("userId");
        toolbar = findViewById(R.id.toolbar);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(()-> {
            users.clear();
            fetchData();
        });

        RecyclerView recyclerView = findViewById(R.id.follow_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);

        noResultFoundMessage = findViewById(R.id.no_result_found);

        fetchData();

        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    private void fetchData(){
        if(type.equals(FOLLOWINGS)){
            fetchFollowings();

        }
        else if (type.equals(FOLLOWERS)){
            fetchFollowers();

        }
    }

    private void fetchFollowers() {
        Call call = userService.getFollowers(token, userId);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    String responseData = response.body().string();

                    Type userListType = new TypeToken<List<User>>(){}.getType();
                    List<User> followers = gson.fromJson(responseData, userListType);

                    FollowActivity.this.runOnUiThread(()-> {
                        if(!followers.isEmpty()) {
                            users.addAll(followers);
                            totalFollower = followers.size();
                            toolbar.setTitle("Người theo dõi"+" : " + totalFollower);

                            userAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);


                            noResultFoundMessage.setVisibility(View.GONE);
                        }
                        else {
                            noResultFoundMessage.setVisibility(View.VISIBLE);
                        }

                    });
                }
            }
        });
    }
    private void fetchFollowings() {
        Call call = userService.getFollowings(token, userId);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200) {
                    String responseData = response.body().string();

                    Type userListType = new TypeToken<List<User>>(){}.getType();
                    List<User> followingUsers = gson.fromJson(responseData, userListType);

                    FollowActivity.this.runOnUiThread(()-> {
                        if(!followingUsers.isEmpty()) {
                            users.addAll(followingUsers.stream().peek(user -> user.setFollowed(true)).collect(Collectors.toList()));
                            totalFollowing = followingUsers.size();
                            toolbar.setTitle("Đang theo dõi"+" : " + totalFollowing);

                            userAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);

                            noResultFoundMessage.setVisibility(View.GONE);
                        }
                        else{
                            noResultFoundMessage.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }
}
