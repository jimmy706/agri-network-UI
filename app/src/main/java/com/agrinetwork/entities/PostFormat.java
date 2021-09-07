package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostFormat {
    REGULAR("REGULAR"),
    SELL("SELL"),
    PLAN("PLAN");

    private String label;
}
