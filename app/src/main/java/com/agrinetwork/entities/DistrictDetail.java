package com.agrinetwork.entities;

import java.util.List;

import lombok.Data;

@Data
public class DistrictDetail extends District{
    public List<Ward> wards;
}
