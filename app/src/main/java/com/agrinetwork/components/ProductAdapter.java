package com.agrinetwork.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.R;
import com.agrinetwork.entities.Product;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private final List<Product> products;
    private final Picasso picasso = Picasso.get();

    private final NumberFormat numberFormat;


    public ProductAdapter(List<Product> products, Context context) {
        this.products = products;
        this.context = context;
        this.numberFormat = NumberFormat.getCurrencyInstance();
        this.numberFormat.setCurrency(Currency.getInstance("VND"));
        this.numberFormat.setMaximumFractionDigits(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_product_item, parent, false);
        return new ProductAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        List<String> thumbnails = product.getThumbnails();
        if(thumbnails != null && !thumbnails.isEmpty()) {
            picasso.load(thumbnails.get(0))
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.thumbnail);
        }

        String productName = product.getName();
        holder.name.setText(productName);

        double productPrice = product.getPrice();
        holder.price.setText(numberFormat.format(productPrice));

        int productViews = product.getNumberOfViews();
        holder.views.setText(Integer.toString(productViews));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name, price, views;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thumbnail = itemView.findViewById(R.id.product_thumbnail);
            this.name = itemView.findViewById(R.id.product_name);
            this.price = itemView.findViewById(R.id.product_price);
            this.views = itemView.findViewById(R.id.product_views);
        }
    }
}
