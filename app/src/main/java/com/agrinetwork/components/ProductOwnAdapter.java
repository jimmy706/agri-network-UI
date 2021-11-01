package com.agrinetwork.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.agrinetwork.R;
import com.agrinetwork.entities.Product;

import java.util.List;

public class ProductOwnAdapter extends ProductAdapter  {

    public ProductOwnAdapter(List<Product> products, Context context) {
        super(products, context);
    }
    @NonNull

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_own, parent, false);
        return new ProductAdapter.ViewHolder(view);
    }


}
