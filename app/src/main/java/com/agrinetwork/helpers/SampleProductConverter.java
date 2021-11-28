package com.agrinetwork.helpers;

import com.agrinetwork.entities.plan.HarvestProduct;
import com.agrinetwork.entities.plan.Plan;
import com.agrinetwork.entities.product.Product;
import com.agrinetwork.entities.product.SampleProduct;

public class SampleProductConverter {
    private final SampleProduct sampleProduct;

    public SampleProductConverter(SampleProduct sampleProduct) {
        this.sampleProduct = sampleProduct;
    }

    public Product toProduct(Plan plan) {
        Product product = new Product();
        product.setName(sampleProduct.getName());
        product.setPrice(0);
        product.setCategories(sampleProduct.getCategories());
        product.setThumbnails(sampleProduct.getThumbnails());
        product.setBroadCasted(true);

        HarvestProduct harvestProduct = plan.getResult();
        product.setQuantity(Float.toString(harvestProduct.getQuantity()));
        product.setQuantityType(harvestProduct.getQuantityType());
        return product;
    }
}
