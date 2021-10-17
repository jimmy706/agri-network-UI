package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Topic {
    PRODUCT_REQUEST("Nhu cầu mua hàng");
    private final String label;

}
