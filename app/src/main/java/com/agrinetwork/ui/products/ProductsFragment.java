package com.agrinetwork.ui.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.agrinetwork.R;
import com.agrinetwork.decorator.GridProductSpacingItemDecorator;
import com.agrinetwork.components.ProductAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.decorator.HorizontalProductSpacingItemDecorator;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.Product;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.service.CategoryService;
import com.agrinetwork.service.ProductService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class ProductsFragment extends Fragment {
    private ChipGroup chipGroup;
    private CategoryService categoryService;
    private ProductService productService;
    private String token;
    private ProductAdapter productAdapter;

    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<Product> products = new ArrayList<>();
    private final List<ProductCategory> productCategoryList = new ArrayList<>();

    private RecyclerView forYouProductList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);
        chipGroup = view.findViewById(R.id.category_group);

        productAdapter = new ProductAdapter(products, getContext());
        forYouProductList = view.findViewById(R.id.recommended_products_for_you);

        forYouProductList.setAdapter(productAdapter);
        forYouProductList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        forYouProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        categoryService = new CategoryService(getContext());
        productService = new ProductService(getContext());

        fetchCategory();
        fetchProducts();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchProducts() {
        Future<PaginationResponse<Product>> productsFuture = executor.submit(() -> {
            Call call = productService.searchProducts(token, new ProductService.SearchProductCriteria());
            Response response = call.execute();
           if(response.code() == 200) {
               Type paginateProductType = new TypeToken<PaginationResponse<Product>>(){}.getType();
               String responseJson = response.body().string();

               return gson.fromJson(responseJson, paginateProductType);
           }
           return null;
        });

        try {
            PaginationResponse<Product> productPaginationResponse = productsFuture.get(6000, TimeUnit.MILLISECONDS);
            if(productPaginationResponse != null) {
                int oldIndex = products.size() - 1;
                products.addAll(productPaginationResponse.getDocs());
                productAdapter.notifyItemRangeInserted(oldIndex, products.size() - 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchCategory(){
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
                            chip.setChipBackgroundColorResource(R.color.white);
                            chip.setTextSize(14);
                            chip.setTextColor(R.color.black);
                            chip.setChipStrokeColor(ColorStateList.valueOf(R.color.black));
                            chip.setChipStrokeWidth(2);

                            chipGroup.addView(chip);
                        }
                    });
                }
            }
        });
    }

}