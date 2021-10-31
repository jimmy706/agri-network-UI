package com.agrinetwork.ui.plan.listener;

import com.agrinetwork.entities.plan.PlanDetail;

import java.util.List;

public interface SubmitPlanDetailsListener {
    void onSubmit(List<PlanDetail> planDetails);
}
