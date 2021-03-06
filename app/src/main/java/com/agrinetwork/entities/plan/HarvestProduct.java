package com.agrinetwork.entities.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HarvestProduct {
    private String name;
    private float quantity;
    private String quantityType;
}
