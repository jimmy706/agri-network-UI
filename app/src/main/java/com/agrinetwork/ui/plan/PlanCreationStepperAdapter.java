package com.agrinetwork.ui.plan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.agrinetwork.R;
import com.agrinetwork.entities.plan.HarvestProduct;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.ui.plan.data.PlanTimestamp;
import com.agrinetwork.ui.plan.listener.OnStepDone;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class PlanCreationStepperAdapter extends AbstractFragmentStepAdapter {
    @Getter
    private final Plan plan;
    private final OnStepDone onStepDone;

    private PlanDetailCreationFragment planDetailCreationFragment = new PlanDetailCreationFragment(this::onSubmitPlanDetails);

    public PlanCreationStepperAdapter(@NonNull FragmentManager fm, @NonNull Context context, OnStepDone onStepDone) {
        super(fm, context);
        this.plan = new Plan();
        this.plan.setPlantDetails(Collections.emptyList());
        this.onStepDone = onStepDone;
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return new PlanTimestampCreationFragment(this::onSubmitPlanTimestamp);
            case 1:
                return planDetailCreationFragment;
            case 2:
                return new PlanResultFragment(this::onSubmitResult);
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(int position) {

        switch (position) {
            case 0:
                return new StepViewModel.Builder(context)
                        .setEndButtonVisible(false)
                        .setBackButtonVisible(false)
                        .setTitle(R.string.plan_creation_step_1_title)
                        .create();
            case 1:
                return new StepViewModel.Builder(context)
                        .setEndButtonVisible(false)
                        .setBackButtonVisible(false)
                        .setTitle(R.string.plan_creation_step_2_title)
                        .create();
            case 2:
                return new StepViewModel.Builder(context)
                    .setEndButtonVisible(false)
                    .setBackButtonVisible(false)
                    .setTitle(R.string.plan_creation_step_3_title)
                    .create();
        }
        return super.getViewModel(position);
    }

    private void onSubmitPlanTimestamp(PlanTimestamp planTimestamp) {
        plan.setFrom(planTimestamp.getFrom());
        plan.setTo(planTimestamp.getTo());
        plan.setName(planTimestamp.getName());
        onStepDone.onDone(0);
        planDetailCreationFragment.setStartPlanDate(planTimestamp.getFrom());
        planDetailCreationFragment.setEndPlanDate(planTimestamp.getTo());
    }

    private void onSubmitPlanDetails(List<PlanDetail> planDetails) {
        plan.setPlantDetails(planDetails);
        onStepDone.onDone(1);
    }

    private void onSubmitResult(HarvestProduct harvestProduct) {
        plan.setResult(harvestProduct);
        onStepDone.onDone(2);
    }


}
