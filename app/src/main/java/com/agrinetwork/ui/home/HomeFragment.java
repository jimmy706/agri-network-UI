package com.agrinetwork.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
    private boolean hasNext = false;
    private int page = 1;
    private int limit = Variables.DEFAULT_LIMIT_POST;
    private final List<PostItem> posts = new ArrayList<>();
    private final List<Boolean> loadedPostItems = new ArrayList<>();

    private PostAdapter postAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout refreshLayout;
    private TextView emptyPostMessage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        postService = new PostService(getContext());

        postAdapter = new PostAdapter(posts,getActivity());

        getTokenFromSharedPreference();

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        emptyPostMessage = binding.emptyPostMessage;

        linearLayoutManager = new LinearLayoutManager(getActivity());

        RecyclerView recyclerView = binding.feed;
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(postAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                getPostCommentAndReactionCount();
                int lastVisibleIndex = linearLayoutManager.findLastVisibleItemPosition();

                if(hasNext && lastVisibleIndex >= posts.size() - 3) {
                    loadMorePosts();
                }
            }
        });

        refreshLayout = binding.refreshLayout;
        refreshLayout.setOnRefreshListener(() -> {
            page = 1;
            posts.clear();
            loadedPostItems.clear();
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
                            if(responseData.getDocs().isEmpty()) {
                                refreshLayout.setVisibility(View.GONE);
                                emptyPostMessage.setVisibility(View.VISIBLE);
                            }
                            else {
                                refreshLayout.setVisibility(View.VISIBLE);
                                emptyPostMessage.setVisibility(View.GONE);

                                posts.addAll(responseData.getDocs());
                                for(int i = 0; i < posts.size(); i++)
                                    loadedPostItems.add(false);

                                postAdapter.notifyDataSetChanged();
                                refreshLayout.setRefreshing(false);

                                hasNext = responseData.isHasNextPage();

                                getPostCommentAndReactionCount();
                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMorePosts() {
        page += 1;
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
                            for(int i = 0; i < posts.size(); i++)
                                loadedPostItems.add(false);

                            postAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);

                            hasNext = responseData.isHasNextPage();
                        });
                    }
                }
            });
        }
    }

    private void getPostCommentAndReactionCount() {

        if(posts.size() > 1) {
            int firstVisibleIndex = linearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleIndex = linearLayoutManager.findLastVisibleItemPosition();

            if(firstVisibleIndex != -1 && lastVisibleIndex != -1) {
                PostItem postItemLast = posts.get(lastVisibleIndex);
                PostItem postItemFirst = posts.get(firstVisibleIndex);

                boolean shouldLoadedPostItemFirst = !loadedPostItems.get(firstVisibleIndex);
                if(shouldLoadedPostItemFirst) {
                    fetchPostCommentCountAndReactionCount(postItemFirst, firstVisibleIndex);
                }

                boolean shouldLoadPostItemLast = !loadedPostItems.get(lastVisibleIndex);
                if(shouldLoadPostItemLast) {
                    fetchPostCommentCountAndReactionCount(postItemLast, lastVisibleIndex);
                }
            }
            else {
                fetchPostCommentCountAndReactionCount(posts.get(0), 0);
            }
        }


    }

    private void fetchPostCommentCountAndReactionCount(PostItem postItem, int postPosition) {
        Call call = postService.getCommentsAndReactionsCountFromPost(token, postItem.get_id());
        loadedPostItems.set(postPosition, true);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String body = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    int countComments = jsonObject.getInt("countComments");
                    int countReactions = jsonObject.getInt("countReactions");
                    boolean isLiked = jsonObject.getBoolean("isLiked");

                   getActivity().runOnUiThread(() -> {

                       postItem.setNumberOfComments(countComments);
                       postItem.setNumberOfReactions(countReactions);
                       postItem.setLiked(isLiked);

                       postAdapter.notifyItemChanged(postPosition);
                   });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

}