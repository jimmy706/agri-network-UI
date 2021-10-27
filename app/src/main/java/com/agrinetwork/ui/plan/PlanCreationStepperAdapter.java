package com.agrinetwork.ui.plan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.agrinetwork.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

public class PlanCreationStepperAdapter extends AbstractFragmentStepAdapter {

    public PlanCreationStepperAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
        super(fm, context);
    }

    @Override
    public Step createStep(int position) {
        switch (position) {
            case 0:
                return new PlanTimestampCreationFragment();
            case 1:
                return new PlanDetailCreationFragment();
        }

        return new PlanTimestampCreationFragment();
    }

    @Override
    public int getCount() {
        return 2;
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
        }
        return super.getViewModel(position);
    }
}
