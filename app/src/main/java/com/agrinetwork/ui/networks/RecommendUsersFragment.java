package com.agrinetwork.ui.networks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.agrinetwork.service.RecommendService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RecommendUsersFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private final List<User> users = new ArrayList<>();
    private UserAdapter userAdapter;
    private RecommendService recommendService;
    private String token;
    private String title;

    private SwipeRefreshLayout refreshLayout;

    public RecommendUsersFragment() {
    }

    public static RecommendUsersFragment newInstance(String title) {
        RecommendUsersFragment fragment = new RecommendUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_recommend_users, container, false);

        recommendService = new RecommendService(getActivity());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        userAdapter = new UserAdapter(users, getActivity());

        refreshLayout = root.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(()-> {
            users.clear();
            fetchRecommendedUsers();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        RecyclerView recyclerView = root.findViewById(R.id.recommended_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);

        fetchRecommendedUsers();


        return root;
    }

    private void fetchRecommendedUsers() {
        Call call = recommendService.getRecommendedUsers(token);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    Gson gson = new Gson();
                    String responseData = response.body().string();

                    Type userListType = new TypeToken<List<User>>(){}.getType();
                    List<User> userList = gson.fromJson(responseData, userListType);

                   getActivity().runOnUiThread(()-> {
                       users.addAll(userList);
                       userAdapter.notifyDataSetChanged();
                       refreshLayout.setRefreshing(false);
                   });
                }
            }
        });
    }

}