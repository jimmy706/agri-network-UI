package com.agrinetwork.ui.plan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.agrinetwork.R;
import com.agrinetwork.components.PlanDetailAdapter;
import com.agrinetwork.components.dialog.CreatePlanDetailDialog;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.service.CategoryService;
import com.agrinetwork.ui.plan.listener.SubmitPlanDetailsListener;
import com.google.android.material.button.MaterialButton;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

public class PlanDetailCreationFragment extends Fragment implements Step {

    private CategoryService categoryService;
    private final List<PlanDetail> planDetails = new ArrayList<>();
    private final SubmitPlanDetailsListener submitPlanDetailsListener;
    private PlanDetailAdapter planDetailAdapter;

    private ImageButton addPlanStepBtn;
    private RecyclerView planDetailList;
    private MaterialButton submitBtn;

    public PlanDetailCreationFragment(SubmitPlanDetailsListener submitPlanDetailsListener) {
        this.submitPlanDetailsListener = submitPlanDetailsListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_detail_creation, container, false);

        categoryService = new CategoryService(getContext());

        addPlanStepBtn = view.findViewById(R.id.add_plan_step_btn);
        addPlanStepBtn.setOnClickListener(v -> {
            CreatePlanDetailDialog dialog = new CreatePlanDetailDialog(getContext(), this::onAddNewPlanDetail);
            dialog.show();
        });

        planDetailAdapter = new PlanDetailAdapter(getContext(), planDetails);
        planDetailList = view.findViewById(R.id.plan_details);
        planDetailList.setAdapter(planDetailAdapter);
        planDetailList.setLayoutManager(new LinearLayoutManager(getContext()));

        submitBtn = view.findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(v -> {
            submitPlanDetailsListener.onSubmit(planDetails);
        });

        return view;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }

    private void onAddNewPlanDetail(PlanDetail planDetail) {
        this.planDetails.add(planDetail);
        planDetailAdapter.notifyItemInserted(this.planDetails.size() - 1);
    }
}