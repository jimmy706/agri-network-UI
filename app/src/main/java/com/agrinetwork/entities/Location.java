package com.agrinetwork.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private double lat;
    private double lng;

    public boolean isValid() {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
