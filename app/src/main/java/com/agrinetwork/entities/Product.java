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
    private String price;
    private String quantityType;
    private User owner;
    private Date createdDate;
    private int view;
    private List<String> thumbnails;

}
