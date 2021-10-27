package com.agrinetwork.entities.plan;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanDetail {
    private String name;
    private Date from;
    private Date to;
    private List<Needed> neededFactors;
}
