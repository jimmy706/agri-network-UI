package com.agrinetwork.entities.plan;

import com.agrinetwork.entities.product.SampleProduct;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Plan {
    private String name;
    private String _id;
    private Date from;
    private Date to;
    private boolean expired;
    private List<PlanDetail> plantDetails;
    private HarvestProduct result;
    private float progress;
    private String status;
    private List<SampleProduct> sampleResults;
}
