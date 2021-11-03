package com.agrinetwork.components;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrinetwork.ProductsActivity;
import com.agrinetwork.R;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.service.CategoryService;

import java.util.List;

public class ProductCategoryMenuAdapter extends RecyclerView.Adapter<ProductCategoryMenuAdapter.ViewHolder>{
    private final List<ProductCategory> categoryList;
    private final Context context;
    private final  CategoryService categoryService;


    public ProductCategoryMenuAdapter(List<ProductCategory> categoryList, Context context){
        this.categoryList = categoryList;
        this.context = context;
        this.categoryService = new CategoryService(context);

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View resultView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_in_navigation, parent, false);
        return new ProductCategoryMenuAdapter.ViewHolder(resultView);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       final ProductCategory item = categoryList.get(position);


        String nameCategory = item.getName();
        holder.category.setText(nameCategory);

        holder.category.setOnClickListener(v->{
            String idCategory = item.get_id();
            String name = item.getName();
            Toast.makeText(context, "id:"+ idCategory, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ProductsActivity.class);
            intent.putExtra("idCategory",idCategory);
            intent.putExtra("nameCategory",name);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final  TextView category ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.category_item);
        }
    }

}
