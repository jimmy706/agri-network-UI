package com.agrinetwork.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.agrinetwork.R;
import com.agrinetwork.components.PlanAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.service.PlanService;
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


public class OwnPlansFragment extends Fragment {

    private String token;
    private String currentUserId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private PlanService planService;
    private final List<Plan> plans = new ArrayList<>();
    private PlanAdapter planAdapter;

    private RecyclerView planList;
    public OwnPlansFragment() {
    }


    public static OwnPlansFragment newInstance() {
        return new OwnPlansFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        currentUserId = sharedPref.getString(Variables.CURRENT_LOGIN_USER_ID, "");

        View view = inflater.inflate(R.layout.fragment_own_plans, container, false);
        planService = new PlanService(getContext());

        planList = view.findViewById(R.id.plan_list);
        planAdapter = new PlanAdapter(getContext(), plans);
        planList.setAdapter(planAdapter);
        planList.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchData();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchData() {
        PlanService.SearchPlanCriteria criteria = new PlanService.SearchPlanCriteria(false);
        criteria.setOwner(currentUserId);

        Future<List<Plan>> futurePlans = executorService.submit(() -> {
            Call call = planService.searchPlan(token, criteria);
            Response response = call.execute();
            if (response.code() == 200) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Plan>>(){}.getType();
                return gson.fromJson(response.body().string(), type);
            }

            return null;
        });

        try {
            List<Plan> fetchResult = futurePlans.get();
            if(fetchResult != null) {
                plans.addAll(fetchResult);
                planAdapter.notifyDataSetChanged();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}