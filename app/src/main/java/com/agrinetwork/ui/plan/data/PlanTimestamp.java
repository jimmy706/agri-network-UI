package com.agrinetwork.ui.plan.data;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanTimestamp {
    private String name;
    private Date from;
    private Date to;
}
