package com.agrinetwork.helpers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.plan.PlanDetail;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.N)
public class AttributesToPlanConverter {
    private static final String NAME_FIELD = "name";
    private static final String FROM_FIELD = "from";
    private static final String TO_FIELD = "to";
    private static final String PLAN_DETAILS_FIELD = "planDetails";
    private static final String DOT = ".";
    private static final String COUNT_STEP_FIELD = "countStep";
    private final Gson gson = new Gson();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private Map<String, String> attributes;

    public AttributesToPlanConverter(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Plan toPlan() {
        for(Map.Entry<String, String> entry : attributes.entrySet()) {
            System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
        }
        Plan plan = new Plan();
        plan.setName(attributes.getOrDefault(NAME_FIELD, ""));
        if (attributes.containsKey(FROM_FIELD) && attributes.containsKey(TO_FIELD)) {
           try {
               Date fromDate = sdf.parse(attributes.get(FROM_FIELD));
               Date toDate = sdf.parse(attributes.get(TO_FIELD));
               plan.setFrom(fromDate);
               plan.setTo(toDate);
           } catch (Exception e) {
               e.printStackTrace();
           }
        }
        plan.setPlantDetails(getPlanDetailsFromAttributes());
        return plan;
    }

    private List<PlanDetail> getPlanDetailsFromAttributes() {
        List<PlanDetail> planDetails = new ArrayList<>();
        int countStep = Integer.parseInt(attributes.getOrDefault(COUNT_STEP_FIELD, "0"));

        if(countStep > 0) {
            for(int i = 0; i < countStep; i++) {
                planDetails.add(getPlanDetailFromAttributes(i));
            }
        }
        return planDetails;
    }

    private PlanDetail getPlanDetailFromAttributes(int index) {
        PlanDetail result = new PlanDetail();
        result.setNeededFactors(Collections.emptyList());
        result.setName(attributes.getOrDefault(PLAN_DETAILS_FIELD + DOT + index + DOT + NAME_FIELD, ""));
        try {
            result.setFrom(sdf.parse(attributes.get(PLAN_DETAILS_FIELD + DOT + index + DOT + FROM_FIELD)));
            result.setFrom(sdf.parse(attributes.get(PLAN_DETAILS_FIELD + DOT + index + DOT + TO_FIELD)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
