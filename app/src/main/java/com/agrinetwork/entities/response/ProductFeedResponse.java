package com.agrinetwork.entities.response;

import com.agrinetwork.entities.product.Product;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductFeedResponse {
    private List<Product> nearby;
    private List<Product> fromFriends;
    private List<Product> popular;
    private List<Product> maybeInterest;
}
