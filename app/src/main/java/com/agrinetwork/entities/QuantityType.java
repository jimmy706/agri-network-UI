package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuantityType {

    POUND("Tấn"),
    WEIGHT("Tạ"),
    STONE ("Yến"),
    KG ("Kg"),
    GRAM("Gram"),
    REGULAR("Cái");

    private final String label;
}
