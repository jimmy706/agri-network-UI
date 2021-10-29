package com.agrinetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.agrinetwork.ui.plan.PlanCreationStepperAdapter;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

public class CreatePlanActivity extends AppCompatActivity implements StepperLayout.StepperListener {
    private StepperLayout stepperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_plan);

        stepperLayout = findViewById(R.id.stepper);
        stepperLayout.setAdapter(new PlanCreationStepperAdapter(getSupportFragmentManager(), this));
        stepperLayout.setListener(this);
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
}