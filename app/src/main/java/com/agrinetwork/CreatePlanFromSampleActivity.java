package com.agrinetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.agrinetwork.helpers.SessionStatusRetriever;
import com.agrinetwork.service.PlanService;
import com.agrinetwork.ui.plan.sample.PlanCreationFromSampleStepperAdapter;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.io.IOException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreatePlanFromSampleActivity extends AppCompatActivity implements StepperLayout.StepperListener {

    private PlanCreationFromSampleStepperAdapter stepperAdapter;

    private StepperLayout stepperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan_from_sample);

        stepperAdapter = new PlanCreationFromSampleStepperAdapter(getSupportFragmentManager(), this, this::onStepDone);
        stepperLayout = findViewById(R.id.stepper);
        stepperLayout.setAdapter(stepperAdapter);
        stepperLayout.setListener(this);
    }

    private void onStepDone(int step) {
        if (step != 1) {
            stepperLayout.setCurrentStepPosition(step + 1);
        }
    }

    @Override
    public void onCompleted(View completeButton) {
        SessionStatusRetriever sessionStatusRetriever = new SessionStatusRetriever(this);
        String token = sessionStatusRetriever.getToken();

        if (token != null && !token.isEmpty()) {
            PlanService planService = new PlanService(this);
            String sampleId = stepperAdapter.getPickedSampleId();
            Date startDate = stepperAdapter.getStartDate();
            Call call = planService.addPlanFromSample(token, sampleId, startDate);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() == 201) {
                        CreatePlanFromSampleActivity.this.runOnUiThread(() -> {
                            Toast.makeText(CreatePlanFromSampleActivity.this, "Tạo kế hoạch sản xuất thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreatePlanFromSampleActivity.this, UserFeedActivity.class));
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onError(VerificationError verificationError) {

    }

    @Override
    public void onStepSelected(int newStepPosition) {

    }

    @Override
    public void onReturn() {

    }
}