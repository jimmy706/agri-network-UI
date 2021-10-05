package com.agrinetwork.entities;

import java.util.List;

import lombok.Data;

@Data
public class ProvinceDetail extends Place{
    private List<District> districts;
}
