package com.agrinetwork.ui.plan.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agrinetwork.R;
import com.agrinetwork.components.PlanDetailAdapter;
import com.agrinetwork.components.SampleProductAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.decorator.HorizontalProductSpacingItemDecorator;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.plan.HarvestProduct;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanDetail;
import com.agrinetwork.entities.product.SampleProduct;
import com.agrinetwork.service.PlanService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Setter;
import okhttp3.Call;
import okhttp3.Response;

public class GeneratePlanFromSampleFragment extends Fragment implements Step {

    @Setter
    private String planSampleId;

    @Setter
    private Date startDate;

    private String token;

    private PlanService planService;

    private Plan plan = null;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView planName, harvestProductName, quantityIntent, stepCount, dueDate;
    private RecyclerView planDetailList, productSampleList;

    public GeneratePlanFromSampleFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genate_plan_from_sample, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(Variables.ID_TOKEN_LABEL, "");

        planService = new PlanService(getContext());

        planName = view.findViewById(R.id.plan_name);
        harvestProductName = view.findViewById(R.id.harvest_name);
        quantityIntent = view.findViewById(R.id.quantity);
        stepCount = view.findViewById(R.id.plan_detail_count);
        planDetailList = view.findViewById(R.id.plan_details);
        dueDate = view.findViewById(R.id.plan_duedate);
        productSampleList = view.findViewById(R.id.sample_products);

        return view;
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {
        if (planSampleId != null && startDate != null) {
            fetchPlanSample();
        }
    }

    private void fetchPlanSample() {
        Future<Plan> future = executor.submit(() -> {
            Call call = planService.getPlanSampleById(token, planSampleId);
            Response response = call.execute();
            if (response.code() == 200) {
                Gson gson = new Gson();
                Type type = new TypeToken<Plan>(){}.getType();
                return gson.fromJson(response.body().string(), type);
            }
            return null;
        });

        try {
            Plan planResponse = future.get();
            if (planResponse != null) {
                plan = planResponse;
                renderOverview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderOverview() {
        if (plan != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(Variables.DATE_FORMAT, new Locale("vi", "VI"));
            planName.setText(plan.getName());

            HarvestProduct harvestProduct = plan.getResult();
            harvestProductName.setText(harvestProduct.getName());
            quantityIntent.setText(harvestProduct.getQuantity() + " " + harvestProduct.getQuantityType());

            String startDate = sdf.format(plan.getFrom());
            String endDate = sdf.format(plan.getTo());
            dueDate.setText(startDate + " - " + endDate);

            List<PlanDetail> planDetails = plan.getPlantDetails();
            stepCount.setText(Integer.toString(planDetails.size()));
            planDetailList.setAdapter(new PlanDetailAdapter(getContext(), planDetails));
            planDetailList.setLayoutManager(new LinearLayoutManager(getContext()));

            List<SampleProduct> sampleProducts = plan.getSampleResults();
            productSampleList.setAdapter(new SampleProductAdapter(getContext(), sampleProducts));
            productSampleList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            productSampleList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));
        }
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}