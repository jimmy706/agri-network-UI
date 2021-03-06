package com.agrinetwork.entities.product;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.User;
import com.agrinetwork.entities.product.Product;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductDetail {
    private String _id;
    private double price;
    private List<ProductCategory> categories;
    private double quantity;
    private String quantityType;
    private Date createdDate;
    private List<String> thumbnails;
    private int numberOfViews;
    private String name;
    private User owner;
    private List<Product> fromOwnerProducts;
    private List<Product> relatedProducts;

}
