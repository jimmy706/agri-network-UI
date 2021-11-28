package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Needed;
import com.agrinetwork.entities.plan.PlanDetail;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlanDetailAdapter extends RecyclerView.Adapter<PlanDetailAdapter.ViewHolder> {

    private final SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
    private final List<PlanDetail> planDetails;
    private final Context context;

    public PlanDetailAdapter(Context context, List<PlanDetail> planDetails) {
        this.context = context;
        this.planDetails = planDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_plan_detail, parent, false);
        return new PlanDetailAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanDetail planDetail = planDetails.get(position);
        holder.name.setText(planDetail.getName());

        holder.stepTitle.setText("Giai đoạn " + (position + 1) +":");

        try {
            String startDate = sdf.format(planDetail.getFrom());
            String endDate = sdf.format(planDetail.getTo());

            holder.dueDate.setText(startDate + " - " + endDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Needed> neededFactors = planDetail.getNeededFactors();
        if(neededFactors != null && !neededFactors.isEmpty()) {
            NeededFactorAdapter neededFactorAdapter = new NeededFactorAdapter(context, planDetail.getNeededFactors());
            holder.neededList.setAdapter(neededFactorAdapter);
            holder.neededList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            holder.neededWrapper.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return planDetails.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name, dueDate, stepTitle;
        private final RecyclerView neededList;
        private final LinearLayout neededWrapper;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.plan_detail_name);
            dueDate = itemView.findViewById(R.id.plan_detail_duedate);
            neededList = itemView.findViewById(R.id.needed_list);
            neededWrapper = itemView.findViewById(R.id.needed_wrapper);
            stepTitle = itemView.findViewById(R.id.step_title);
        }
    }
}
