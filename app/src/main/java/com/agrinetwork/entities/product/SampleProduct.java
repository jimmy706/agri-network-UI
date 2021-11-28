package com.agrinetwork.entities.product;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SampleProduct {
    private String _id;
    private String name;
    private List<String> thumbnails;
    private List<String> categories;
}
