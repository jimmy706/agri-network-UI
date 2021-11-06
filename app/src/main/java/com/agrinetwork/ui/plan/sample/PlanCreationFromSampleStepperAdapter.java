package com.agrinetwork.ui.plan.sample;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.agrinetwork.R;
import com.agrinetwork.ui.plan.listener.OnStepDone;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import java.util.Date;

import lombok.Getter;

public class PlanCreationFromSampleStepperAdapter extends AbstractFragmentStepAdapter {
    private static final int COUNT_STEP = 2;
    private final OnStepDone onStepDoneListener;
    private GeneratePlanFromSampleFragment generatePlanFromSampleFragment;

    @Getter
    private String pickedSampleId;
    @Getter
    private Date startDate;

    public PlanCreationFromSampleStepperAdapter(@NonNull FragmentManager fm, @NonNull Context context, OnStepDone onStepDone) {
        super(fm, context);
        this.onStepDoneListener = onStepDone;
        this.generatePlanFromSampleFragment = new GeneratePlanFromSampleFragment();
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return new PickPlanSampleFragment(this::onPickSample);
            case 1:
                return generatePlanFromSampleFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return COUNT_STEP;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(int position) {
        switch (position) {
            case 0:
                return new StepViewModel.Builder(context)
                        .setTitle(R.string.plan_creation_sample_step_1_title)
                        .create();
            case 1:
                return new StepViewModel.Builder(context)
                        .setTitle(R.string.plan_creation_sample_step_2_title)
                        .create();
        }

        return super.getViewModel(position);
    }

    private void onPickSample(String sampleId, Date startDate) {
        generatePlanFromSampleFragment.setPlanSampleId(sampleId);
        generatePlanFromSampleFragment.setStartDate(startDate);

        this.pickedSampleId = sampleId;
        this.startDate = startDate;
        onStepDoneListener.onDone(0);
    }
}
