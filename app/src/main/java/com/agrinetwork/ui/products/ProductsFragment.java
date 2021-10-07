package com.agrinetwork.ui.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.agrinetwork.R;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.service.CategoryService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ProductsFragment extends Fragment {
    private ChipGroup chipGroup;
    private CategoryService categoryService;
    private String token;
    private final Gson gson = new Gson();
    private final List<ProductCategory> productCategoryList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);
        chipGroup = view.findViewById(R.id.category_group);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        categoryService = new CategoryService(getContext());

        fetchCategory();
        return  view;
    }

    public  void fetchCategory(){
        Call getAllCategory = categoryService.getCategory(token);
        getAllCategory.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getAllCategory, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onResponse(@NonNull Call getAllCategory, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String responseCategory = response.body().string();

                    Type categoryList = new TypeToken<List<ProductCategory>>(){}.getType();
                    List<ProductCategory> productTypes = gson.fromJson(responseCategory,categoryList);

                    getActivity().runOnUiThread(()->{
                        productCategoryList.addAll(productTypes);

                        for(int i = 0; i < productCategoryList.size(); i++){
                            final ProductCategory category = productCategoryList.get(i);
                            String categoryName = category.getName();
                            final Chip chip = new Chip(getContext());
                            chip.setText(categoryName);
                            chip.setChipBackgroundColorResource(R.color.teal_200);
                            chip.setTextSize(16);

                            chipGroup.addView(chip);

                        }

                    });
                }
            }
        });
    }

}