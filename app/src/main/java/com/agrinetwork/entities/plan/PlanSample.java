package com.agrinetwork.entities.plan;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanSample {
    private String _id;
    private String name;
    private long tookTime;
    private List<PlanSampleStep> plantDetails;
    private HarvestProduct result;
}
