package com.agrinetwork.components.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.components.PlanAdapter;
import com.agrinetwork.entities.plan.Plan;

import java.util.List;

public class NotifyExpiredPlanDialog extends Dialog {

    private final List<Plan> plans;
    private final String message;

    public NotifyExpiredPlanDialog(@NonNull Context context, List<Plan> plans, String message) {
        super(context);
        this.plans = plans;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_notify_expired_plans);

        TextView messageText = findViewById(R.id.message);
        messageText.setText(message);

        RecyclerView planList = findViewById(R.id.plan_list);
        planList.setAdapter(new PlanAdapter(getContext(), plans));
        planList.setLayoutManager(new LinearLayoutManager(getContext()));

        Button closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> dismiss());
    }
}
