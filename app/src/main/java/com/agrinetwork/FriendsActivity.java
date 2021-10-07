package com.agrinetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.agrinetwork.components.UserAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PaginationResponse;
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
@RequiresApi(api = Build.VERSION_CODES.O)
public class FriendsActivity extends AppCompatActivity {
    private final List<User> friends = new ArrayList<>();
    private final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private String token;
    private int totalFriends;
    private String userId;
    private boolean hasNext;
    private int page = 1;
    private int limit = 12;
    private UserService userService;
    private UserAdapter userAdapter;

    private RecyclerView friendsRecyclerView;
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        userService = new UserService(this);

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");

        Intent intent = getIntent();
        userId = intent.getExtras().getString("userId");

        toolbar = findViewById(R.id.toolbar);

        userAdapter = new UserAdapter(friends, this, true);

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(() -> {
            page = 1;
            friends.clear();
            fetchUserFriends();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        friendsRecyclerView = findViewById(R.id.friend_list);
        friendsRecyclerView.setLayoutManager(linearLayoutManager);
        friendsRecyclerView.setAdapter(userAdapter);

        friendsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleIndex = linearLayoutManager.findLastVisibleItemPosition();

                if(hasNext && lastVisibleIndex >= (friends.size() - 3)){
                    loadMoreFriends();
                }
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        fetchUserFriends();
    }

    private void fetchUserFriends() {
        Future<List<User>> future = executorService.submit(() -> {
            Call call = userService.getFriends(token, userId, page, limit);
            List<User> responseData = new ArrayList<>();
            Response response = call.execute();
            if(response.code() == 200) {
                Type responseType = new TypeToken<PaginationResponse<User>>(){}.getType();
                PaginationResponse<User> friendsResponse = gson.fromJson(response.body().string(), responseType);
                friends.addAll(friendsResponse.getDocs());
                totalFriends = friendsResponse.getTotalDocs();
                hasNext = friendsResponse.isHasNextPage();
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
        swiper.setRefreshing(false);

        if(!friends.isEmpty()) {
            toolbar.setTitle(getText(R.string.friended) + ": " + totalFriends);
            userAdapter = new UserAdapter(friends, this, true);
            friendsRecyclerView.setAdapter(userAdapter);
            userAdapter.notifyItemRangeChanged(0, friends.size());

        }
    }

    private void loadMoreFriends() {
        page += 1;
        try {
            PaginationResponse<User> response = fetchFriends();
            if(response != null) {
                int oldSize = friends.size();
                friends.addAll(response.getDocs());
                hasNext = response.isHasNextPage();
                userAdapter.notifyItemRangeChanged(oldSize - 1, response.getDocs().size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PaginationResponse<User> fetchFriends() throws Exception {
        Future<PaginationResponse<User>> future = executorService.submit(() -> {
            Call call = userService.getFriends(token, userId, page, limit);
            Response response = call.execute();
            if(response.code() == 200) {
                Type responseType = new TypeToken<PaginationResponse<User>>(){}.getType();
                PaginationResponse<User> friendsResponse = gson.fromJson(response.body().string(), responseType);
                return friendsResponse;
            }
            return null;
        });

        return future.get();
    }
}