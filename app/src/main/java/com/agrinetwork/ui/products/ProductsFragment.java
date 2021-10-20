package com.agrinetwork.ui.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.agrinetwork.ProductsActivity;
import com.agrinetwork.R;
import com.agrinetwork.components.ProductAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.decorator.HorizontalProductSpacingItemDecorator;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.entities.response.ProductFeedResponse;
import com.agrinetwork.service.CategoryService;
import com.agrinetwork.service.RecommendService;
import com.google.android.material.button.MaterialButton;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProductsFragment extends Fragment {

    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<ProductCategory> productCategoryList = new ArrayList<>();
    private final int[] wrapperIds = new int[]{R.id.interest_wrapper, R.id.nearby_wrapper, R.id.from_friends_wrapper, R.id.popular_wrapper};

    private ChipGroup chipGroup;
    private CategoryService categoryService;
    private RecommendService recommendService;
    private String token;
    private ProductFeedResponse productFeed;

    private RecyclerView maybeInterestProductList;
    private RecyclerView nearbyProductList;
    private RecyclerView fromFriendsProductList;
    private RecyclerView popularProductList;

    List<LinearLayout> wrappers = new ArrayList<>();

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
        getLayouts(view);

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        categoryService = new CategoryService(getContext());
        recommendService = new RecommendService(getContext());

        fetchCategory();
        fetchProductFeed();

        MaterialButton viewMoreBtn = view.findViewById(R.id.view_more_btn);
        viewMoreBtn.setOnClickListener(v -> {
            getContext().startActivity(new Intent(getActivity(), ProductsActivity.class));
        });

        return view;
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

    private void fetchProductFeed() {
        Future<ProductFeedResponse> productFeedResponseFuture = executor.submit(()-> {
            Call call = recommendService.getProductsFeed(token);
            Response response = call.execute();
            if(response.code() == 200) {
                String responseBodyJson = response.body().string();
                Type responseType = new TypeToken<ProductFeedResponse>(){}.getType();

                return gson.fromJson(responseBodyJson, responseType);
            }
            return null;
        });

        try {
            ProductFeedResponse response = productFeedResponseFuture.get();
            if(response != null) {
                productFeed = response;
                renderData();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getLayouts(View view) {
        maybeInterestProductList = view.findViewById(R.id.recommended_products_interest);
        maybeInterestProductList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        maybeInterestProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        nearbyProductList = view.findViewById(R.id.recommended_products_nearby);
        nearbyProductList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        fromFriendsProductList = view.findViewById(R.id.recommended_products_from_friend);
        fromFriendsProductList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        fromFriendsProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        popularProductList = view.findViewById(R.id.recommended_products_popular);
        popularProductList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        popularProductList.addItemDecoration(new HorizontalProductSpacingItemDecorator(20));

        for (int wrapperId : wrapperIds) {
            LinearLayout wrapper = view.findViewById(wrapperId);
            wrappers.add(wrapper);
        }

    }

    private void renderData() {
        if(!productFeed.getMaybeInterest().isEmpty()) {
            ProductAdapter interestProdAdt = new ProductAdapter(productFeed.getMaybeInterest(), getContext());
            maybeInterestProductList.setAdapter(interestProdAdt);
        }
        else {
            wrappers.get(0).setVisibility(View.GONE);
        }

        if(!productFeed.getNearby().isEmpty()) {
            ProductAdapter nearbyProdAdt = new ProductAdapter(productFeed.getNearby(), getContext());
            nearbyProductList.setAdapter(nearbyProdAdt);
        }
        else {
            wrappers.get(1).setVisibility(View.GONE);
        }

        if(!productFeed.getFromFriends().isEmpty()) {
            ProductAdapter fromFriendProdAdt = new ProductAdapter(productFeed.getFromFriends(), getContext());
            fromFriendsProductList.setAdapter(fromFriendProdAdt);
        }
        else {
            wrappers.get(2).setVisibility(View.GONE);
        }

       if(!productFeed.getPopular().isEmpty()) {
           ProductAdapter popularProdAdt = new ProductAdapter(productFeed.getPopular(), getContext());
           popularProductList.setAdapter(popularProdAdt);
       }
       else {
           wrappers.get(3).setVisibility(View.GONE);
       }
    }

}