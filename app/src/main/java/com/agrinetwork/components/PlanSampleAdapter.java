package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.plan.PlanSample;
import com.agrinetwork.interfaces.listeners.OnSamplePlanClickListener;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import lombok.Setter;

public class PlanSampleAdapter extends RecyclerView.Adapter<PlanSampleAdapter.ViewHolder> {
    private static final long DAY_AS_MS = 86400000;
    private final List<PlanSample> planSamples;
    private final Context context;

    @Setter
    private OnSamplePlanClickListener clickListener;

    public PlanSampleAdapter(Context context, List<PlanSample> planSamples) {
        this.context = context;
        this.planSamples = planSamples;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_plan_sample, parent, false);
        return new PlanSampleAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanSample planSample = planSamples.get(position);

        holder.planName.setText(planSample.getName());
        holder.countPlanDetail.setText(""+ planSample.getPlantDetails().size());

        int daySpent = 0;
        long tookTime = planSample.getTookTime();
        if (tookTime >= DAY_AS_MS) {
            daySpent = (int) (tookTime / DAY_AS_MS);
        }
        holder.timeSpend.setText(Integer.toString(daySpent) + " ngày");

        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(v, position, planSample.get_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return planSamples.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView planName, timeSpend, countPlanDetail;
        private final MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            planName = itemView.findViewById(R.id.plan_name);
            timeSpend = itemView.findViewById(R.id.plan_time_spend);
            countPlanDetail = itemView.findViewById(R.id.plan_detail_count);
        }
    }
}
