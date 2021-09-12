package com.agrinetwork.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.components.PostAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.databinding.FragmentHomeBinding;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.service.PostService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class HomeFragment extends Fragment {
    private PostService postService;
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private String token;
    private final List<PostItem> posts = new ArrayList<>();
    private final PostAdapter postAdapter = new PostAdapter(posts);
    private int page = 1;
    private int limit = Variables.DEFAULT_LIMIT_POST;

    private SwipeRefreshLayout refreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        postService = new PostService(getContext());
        getTokenFromSharedPreference();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.feed;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(postAdapter);

        refreshLayout = binding.refreshLayout;
        refreshLayout.setOnRefreshListener(() -> {
            page = 1;

            posts.clear();
            fetchPosts();
        });

        fetchPosts();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getTokenFromSharedPreference() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
    }

    private void fetchPosts() {
        if(token != null && !token.isEmpty()) {
            Call call = postService.getPosts(token, page, limit);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    refreshLayout.setRefreshing(false);
                }

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.code() == 200) {
                        Gson gson = new Gson();
                        String responseBody = response.body().string();

                        Type paginationType = new TypeToken<PaginationResponse<PostItem>>(){}.getType();
                        PaginationResponse<PostItem> responseData = gson.fromJson(responseBody,paginationType);

                        getActivity().runOnUiThread(()-> {
                            posts.addAll(responseData.getDocs());
                            postAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                        });
                    }
                }
            });
        }
    }

}