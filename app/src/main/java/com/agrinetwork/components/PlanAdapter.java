package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.PlanInfoActivity;
import com.agrinetwork.R;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    private final List<Plan> plans;
    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));

    public PlanAdapter(Context context, List<Plan> plans) {
        this.context = context;
        this.plans = plans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_plan, parent, false);
        return new PlanAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plan plan = plans.get(position);
        holder.name.setText(plan.getName());

        try {
            String startDate = sdf.format(plan.getFrom());
            String endDate = sdf.format(plan.getTo());
            holder.dueDate.setText(startDate + " - " + endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.countSteps.setText(Integer.toString(plan.getPlantDetails().size()));

        holder.progress.setProgress((int) (plan.getProgress() * 100));

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlanInfoActivity.class);
            intent.putExtra("planId", plan.get_id());
            context.startActivity(intent);
        });

        holder.status.setText(plan.getStatus());
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView name, dueDate, countSteps, status;
        final ProgressBar progress;
        final MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.plan_name);
            this.dueDate = itemView.findViewById(R.id.plan_duedate);
            this.countSteps = itemView.findViewById(R.id.plan_detail_count);
            this.progress = itemView.findViewById(R.id.progress);
            this.cardView = itemView.findViewById(R.id.card_view);
            this.status = itemView.findViewById(R.id.plan_status);
        }
    }

}
