package com.agrinetwork.ui.networks;

import android.app.Activity;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.agrinetwork.R;
import com.agrinetwork.components.FriendRequestAdapter;
import com.agrinetwork.components.RecommendUserAdapter;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.FriendRequest;
import com.agrinetwork.entities.RecommendUser;
import com.agrinetwork.service.RecommendService;
import com.agrinetwork.service.UserService;
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

    private final Gson gson = new Gson();
    private final List<RecommendUser> users = new ArrayList<>();
    private final List<FriendRequest> friendRequests = new ArrayList<>();
    private RecommendUserAdapter userAdapter;
    private FriendRequestAdapter friendRequestAdapter;
    private RecommendService recommendService;
    private UserService userService;
    private String token;
    private String title;

    private LinearLayout recommendUsersWrapper, pendingFriendRequestsWrapper;

    private TextView noRecommendedUserMessage;

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
        userService = new UserService(getActivity());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        userAdapter = new RecommendUserAdapter(users, getActivity());
        friendRequestAdapter = new FriendRequestAdapter(friendRequests, getActivity());
        friendRequestAdapter.setHandleFriendRequestListener((position, status) -> {
            getActivity().runOnUiThread(()-> {
                friendRequests.remove(position);
                renderPendingFriendRequests();
            });
        });

        recommendUsersWrapper = root.findViewById(R.id.recommended_wrapper);
        pendingFriendRequestsWrapper = root.findViewById(R.id.pending_friend_request);

        LinearLayoutManager recommendUserLayoutMng = new LinearLayoutManager(getActivity());
        RecyclerView recommendedUserRecyclerView = root.findViewById(R.id.recommended_list);
        recommendedUserRecyclerView.setLayoutManager(recommendUserLayoutMng);
        recommendedUserRecyclerView.setAdapter(userAdapter);

        LinearLayoutManager pendingFriendRqLayoutMng = new LinearLayoutManager(getActivity());
        RecyclerView pendingFriendRqRecyclerView = root.findViewById(R.id.friend_request_list);
        pendingFriendRqRecyclerView.setLayoutManager(pendingFriendRqLayoutMng);
        pendingFriendRqRecyclerView.setAdapter(friendRequestAdapter);

        noRecommendedUserMessage = root.findViewById(R.id.no_recommended_user);

        fetchPendingFriendRequest();
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
                    String responseData = response.body().string();

                    Type userListType = new TypeToken<List<RecommendUser>>(){}.getType();
                    List<RecommendUser> userList = gson.fromJson(responseData, userListType);

                    Activity currentActivity = getActivity();
                    if(currentActivity != null) {
                        currentActivity.runOnUiThread(()-> {
                            if(!userList.isEmpty()) {
                                users.addAll(userList);
                            }
                            renderRecommendedUsers();
                        });
                    }
                }
            }
        });
    }

    private void fetchPendingFriendRequest() {
        Call call = userService.getFriendRequests(token);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                Type friendRequestListType = new TypeToken<List<FriendRequest>>(){}.getType();

                List<FriendRequest> friendRequestsResponse = gson.fromJson(responseData, friendRequestListType);
                getActivity().runOnUiThread(() -> {
                    if(!friendRequestsResponse.isEmpty()) {
                        friendRequests.addAll(friendRequestsResponse);
                    }
                    renderPendingFriendRequests();
                });
            }
        });
    }

    private void renderRecommendedUsers() {
        userAdapter.notifyDataSetChanged();
        if(!users.isEmpty()) {
            recommendUsersWrapper.setVisibility(View.VISIBLE);
        }
        else {
            recommendUsersWrapper.setVisibility(View.GONE);
        }
        renderEmptyMessage();
    }

    private void renderPendingFriendRequests() {
        friendRequestAdapter.notifyDataSetChanged();
        if(!friendRequests.isEmpty()) {
            pendingFriendRequestsWrapper.setVisibility(View.VISIBLE);
        }
        else {
            pendingFriendRequestsWrapper.setVisibility(View.GONE);
        }
        renderEmptyMessage();
    }

    private void renderEmptyMessage() {
        if(pendingFriendRequestsWrapper.getVisibility() == View.GONE && recommendUsersWrapper.getVisibility() == View.GONE) {
            noRecommendedUserMessage.setVisibility(View.VISIBLE);
        }
        else {
            noRecommendedUserMessage.setVisibility(View.GONE);
        }
    }

}