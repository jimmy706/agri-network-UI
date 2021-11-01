package com.agrinetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agrinetwork.components.PlanAdapter;
import com.agrinetwork.components.PlanDetailAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.plan.HarvestProduct;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.entities.plan.PlanInformation;
import com.agrinetwork.service.PlanService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Response;

public class PlanInfoActivity extends AppCompatActivity {

    private String token;
    private PlanService planService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private TextView planName, userDisplayName, harvestProductName, quantityIntent, stepCount, progressPercent;
    private ProgressBar progressBar;
    private CircleImageView userAvatar;
    private RecyclerView planDetailList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_info);

        Intent currentIntent = getIntent();
        String planId = currentIntent.getExtras().getString("planId");

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");

        planService = new PlanService(this);

        planName = findViewById(R.id.plan_name);
        userDisplayName = findViewById(R.id.user_name);
        userAvatar = findViewById(R.id.user_avatar);
        harvestProductName = findViewById(R.id.harvest_name);
        quantityIntent = findViewById(R.id.quantity);
        stepCount = findViewById(R.id.plan_detail_count);
        progressPercent = findViewById(R.id.progress_percent);
        progressBar = findViewById(R.id.progress);
        planDetailList = findViewById(R.id.plan_details);

        fetchPlanDetail(planId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void fetchPlanDetail(String planId) {
        Future<PlanInformation> planFuture = executorService.submit(() -> {
            Call call = planService.getPlanById(token, planId);
            Response response = call.execute();
            if(response.code() == 200) {
                Gson gson = new Gson();
                String jsonPlan = response.body().string();
                Type type = new TypeToken<PlanInformation>(){}.getType();

                return gson.fromJson(jsonPlan, type);
            }

            return null;
        });

        try {
            PlanInformation plan = planFuture.get();
            if (plan != null) {
                renderData(plan);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderData(PlanInformation plan) {
        planName.setText(plan.getName());

        User owner = plan.getOwner();
        String fName = owner.getFirstName();
        String lName = owner.getLastName();
        userDisplayName.setText(fName + " " + lName);

        String avatar = owner.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            Picasso.get().load(avatar)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(userAvatar);
        }

        HarvestProduct harvestProduct = plan.getResult();
        harvestProductName.setText(harvestProduct.getName());
        quantityIntent.setText(harvestProduct.getQuantity() + " " + harvestProduct.getQuantityType());
        System.out.println(plan);
        int progress = (int)(plan.getProgress() * 100);
        progressBar.setProgress(progress);
        progressPercent.setText(progress + "%");

        List<PlanDetail> planDetails = plan.getPlantDetails();
        stepCount.setText(Integer.toString(planDetails.size()));
        planDetailList.setAdapter(new PlanDetailAdapter(this, planDetails));
        planDetailList.setLayoutManager(new LinearLayoutManager(this));
    }
}