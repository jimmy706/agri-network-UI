package com.agrinetwork.entities;

import java.util.Date;



import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Product {
    private String _id;
    private String name;
    private List<String> categories;
    private String quantity;
    private String quantityType;
    private Date createdDate;
    private double price;
    private int numberOfViews;
    private List<String> thumbnails;
    private boolean isBroadCasted;
    private String status;
}
