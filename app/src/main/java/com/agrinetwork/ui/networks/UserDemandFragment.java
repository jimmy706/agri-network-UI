package com.agrinetwork.ui.networks;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.agrinetwork.components.RecommendUserDemandAdapter;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.RecommendUserDemand;

import com.agrinetwork.service.RecommendService;

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


public class UserDemandFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private final Gson gson = new Gson();

    private final List<RecommendUserDemand> recommendUserDemandList = new ArrayList<>();
    private RecommendUserDemandAdapter recommendUserDemandAdapter;
    private RecommendService recommendService;
    private String token;
    private SwipeRefreshLayout refreshLayout;
    private String title;
    private TextView noResultFoundMessage;
    private LinearLayoutManager LinearLayoutManager;
    private RecyclerView recommendedUserDemandRecyclerView;
    private TextView noResult;

    public UserDemandFragment() {
    }

    public static UserDemandFragment newInstance(String title) {
        UserDemandFragment fragment = new UserDemandFragment();
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
        View root = inflater.inflate(R.layout.fragment_recommend_user_demand, container, false);
        recommendService = new RecommendService(getActivity());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        recommendedUserDemandRecyclerView = root.findViewById(R.id.list_user_demand);
        recommendUserDemandAdapter = new RecommendUserDemandAdapter(recommendUserDemandList,getActivity());
         LinearLayoutManager = new LinearLayoutManager(getActivity());
         recommendedUserDemandRecyclerView.setLayoutManager(LinearLayoutManager);
        recommendedUserDemandRecyclerView.setAdapter(recommendUserDemandAdapter);

        noResult = root.findViewById(R.id.no_result_found);

        fetchRecommendUserDemand();

        return root;

    }

    private void fetchRecommendUserDemand(){
        Call call = recommendService.getUserDemand(token);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code()==200){
                    String responseUserDemand = response.body().string();
                    System.out.println(responseUserDemand);

                    Type userDemandType = new TypeToken<List<RecommendUserDemand>>(){}.getType();
                    List<RecommendUserDemand> userDemandList = gson.fromJson(responseUserDemand,userDemandType);

                        getActivity().runOnUiThread(()-> {
                            if(!userDemandList.isEmpty()) {
                                recommendUserDemandList.addAll(userDemandList);
                                recommendUserDemandAdapter.notifyDataSetChanged();
                            }
                            else{
                                noResult.setVisibility(View.VISIBLE);
                            }

                        });

                }
            }
        });
    }






}