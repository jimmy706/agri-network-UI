package com.agrinetwork.entities.plan;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanSampleStep {
    private String name;
    private long tookTime;
    private List<Needed> neededFactors;
}
