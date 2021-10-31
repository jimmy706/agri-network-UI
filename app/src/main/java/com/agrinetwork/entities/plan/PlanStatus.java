package com.agrinetwork.entities.plan;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanStatus {
    IN_PROGRESS("Đang diễn ra"),
    EXPIRED("Kết thúc"),
    HARVEST("Thu hoạch");
    private final String label;
}
