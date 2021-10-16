package com.agrinetwork.entities;
<<<<<<< HEAD
import java.util.Date;
=======

>>>>>>> 3643f2ea19bfae5f78b0f26eae71f9622599db51
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Product {
    private String _id;
    private String name;
<<<<<<< HEAD
    private List<String> categories;
    private String quantity;
    private String price;
    private String quantityType;
    private User owner;
    private Date createdDate;
    private int view;
    private List<String> thumbnails;

=======
    private double price;
    private int views;
    private List<String> thumbnails;
>>>>>>> 3643f2ea19bfae5f78b0f26eae71f9622599db51
}
