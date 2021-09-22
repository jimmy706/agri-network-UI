package com.agrinetwork.ui.networks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agrinetwork.R;
import com.agrinetwork.components.UserAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.service.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class MyFollowFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ART_TYPE = "type";

    private final List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private UserService userService;
    private String token;
    private String currentLoginUserId;
    private SwipeRefreshLayout refreshLayout;

    private final Gson gson = new Gson();

    private String title;
    private String type;

    public MyFollowFragment() {
    }

    public static MyFollowFragment newInstance(String title, FollowTabFragmentType tabType) {
        MyFollowFragment fragment = new MyFollowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ART_TYPE, tabType.getType());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            type = getArguments().getString(ART_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_follow, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        currentLoginUserId = sharedPref.getString(Variables.CURRENT_LOGIN_USER_ID, "");

        userService = new UserService(getContext());

        userAdapter = new UserAdapter(users, getContext());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        refreshLayout = root.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(()-> {
            users.clear();
            fetchFollowers();
        });

        RecyclerView recyclerView = root.findViewById(R.id.follow_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);

        fetchData();

        return root;
    }

    private void fetchData() {
        if(type.equals(FollowTabFragmentType.FOLLOWINGS.getType())) {
            fetchFollowings();
        }
        else if(type.equals(FollowTabFragmentType.FOLLOWERS.getType())) {
            fetchFollowers();
        }
    }

    private void fetchFollowers() {
        Call call = userService.getFollowers(token, currentLoginUserId);
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

                    getActivity().runOnUiThread(()-> {
                        users.addAll(followers);
                        userAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }

    private void fetchFollowings() {
        Call call = userService.getFollowers(token, currentLoginUserId);
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

                    getActivity().runOnUiThread(()-> {
                        users.addAll(followingUsers.stream().peek(user -> user.setFollowed(true)).collect(Collectors.toList()));
                        userAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    });
                }
            }
        });
    }
}