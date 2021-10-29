package com.agrinetwork.entities.plan;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Needed {
    private String name;
    private List<String> categories;
    private double[] priceRange;
}
