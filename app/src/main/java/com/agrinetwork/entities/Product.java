package com.agrinetwork.entities;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Product {
    private String _id;
    private String name;
    private double price;
    private int views;
    private List<String> thumbnails;
}
