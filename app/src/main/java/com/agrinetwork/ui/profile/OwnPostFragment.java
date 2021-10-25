package com.agrinetwork.ui.profile;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.components.PostAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.PostItem;
import com.agrinetwork.entities.Product;
import com.agrinetwork.service.PostService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OwnPostFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private String title;
    private PostService postService;
    private int page = 1;
    private String token;
    private boolean hasNext = false;
    private  List<PostItem> postList = new ArrayList<>();
    private int limit = Variables.DEFAULT_LIMIT_POST;
    private String currentLoginUserId;
    private RecyclerView postFromOwn;
    private final Gson gson = new Gson();
    private PaginationResponse<PostItem> postPaginationResponse;
    private PostAdapter postAdapterOwn;
    private TextView showTextNoPostOwn;
    public OwnPostFragment(){

    }

    public static OwnPostFragment newInstance(String title){
        OwnPostFragment ownPostFragment = new OwnPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        ownPostFragment.setArguments(args);
        return ownPostFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceSate){
        View root = inflater.inflate(R.layout.fragment_own_post, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        currentLoginUserId = sharedPref.getString(Variables.CURRENT_LOGIN_USER_ID, "");

        postService = new PostService(getContext());
        postFromOwn = root.findViewById(R.id.list_post_own);

        showTextNoPostOwn = root.findViewById(R.id.no_postOwn);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        postFromOwn.setLayoutManager(linearLayoutManager);
        postAdapterOwn = new PostAdapter(postList, getContext());
        postFromOwn.setAdapter(postAdapterOwn);


        postFromOwn.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleIndex = linearLayoutManager.findLastVisibleItemPosition();
                if(hasNext && lastVisibleIndex >= postList.size() - 1) {
                    loadMorePostsOwn();
                }
            }
        });


        fetchPostFromOwn();

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchPostFromOwn(){
        Call getPostFromOwn = postService.getPostsFromUser(token,currentLoginUserId,page,limit);
        getPostFromOwn.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String postResponse = response.body().string();

                   Type paginationType = new TypeToken<PaginationResponse<PostItem>>(){}.getType();
                  postPaginationResponse = gson.fromJson(postResponse,paginationType);

                   getActivity().runOnUiThread(()->{
                       if(!postPaginationResponse.getDocs().isEmpty()){
                           postList.addAll(postPaginationResponse.getDocs());
                           System.out.println(postList);
                           postAdapterOwn.notifyDataSetChanged();
                           hasNext = postPaginationResponse.isHasNextPage();

                       }
                       else{
                           showTextNoPostOwn.setVisibility(View.VISIBLE);
                       }

                   });
                }

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadMorePostsOwn(){
        page += 1;
        Call getPostFromOwn = postService.getPostsFromUser(token,currentLoginUserId,page,limit);
        getPostFromOwn.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @SuppressLint({"ResourceAsColor", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String postResponse = response.body().string();

                    Type paginationType = new TypeToken<PaginationResponse<PostItem>>(){}.getType();
                    postPaginationResponse = gson.fromJson(postResponse,paginationType);

                    getActivity().runOnUiThread(()->{
                        if(!postPaginationResponse.getDocs().isEmpty()){
                            postList.addAll(postPaginationResponse.getDocs());
                            postAdapterOwn.notifyDataSetChanged();
                            hasNext = postPaginationResponse.isHasNextPage();
                        }

                    });
                }

            }
        });

    }
}
