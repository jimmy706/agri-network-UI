package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserTypes {
    SUPPLIER("Nhà cung cấp"),
    PRODUCER("Hộ sản xuất"),
    BUYER("Người thu mua");

    private final String label;


}
