package com.agrinetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.helpers.SessionStatusRetriever;
import com.agrinetwork.service.PlanService;
import com.agrinetwork.ui.plan.PlanCreationStepperAdapter;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Response;

public class CreatePlanActivity extends AppCompatActivity implements StepperLayout.StepperListener {
    private StepperLayout stepperLayout;
    private PlanCreationStepperAdapter stepperAdapter;
    private PlanService planService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        stepperAdapter = new PlanCreationStepperAdapter(getSupportFragmentManager(), this, this::onStepSubmit);

        stepperLayout = findViewById(R.id.stepper);
        stepperLayout.setAdapter(stepperAdapter);
        stepperLayout.setListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        planService = new PlanService(this);
    }

    @Override
    public void onCompleted(View completeButton) {
    }

    @Override
    public void onError(VerificationError verificationError) {
    }

    @Override
    public void onStepSelected(int newStepPosition) {

    }

    @Override
    public void onReturn() {
        finish();
    }

    private void onStepSubmit(int step) {
        if (step != 2) {
            stepperLayout.setCurrentStepPosition(step + 1);
        } else {
            Plan plan = stepperAdapter.getPlan();
            executor.submit(() -> {
                Call call = planService.addPlan(token, plan);
                try {
                    Response response = call.execute();
                    if (response.code() == 201) {
                       CreatePlanActivity.this.runOnUiThread(() -> {
                           Toast.makeText(CreatePlanActivity.this, "Tạo kế hoạch sản xuất thành công!", Toast.LENGTH_SHORT).show();
                           startActivity(new Intent(CreatePlanActivity.this, UserFeedActivity.class));
                       });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

}