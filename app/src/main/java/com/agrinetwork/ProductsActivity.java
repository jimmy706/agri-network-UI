package com.agrinetwork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.components.ProductOwnAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.Product;
import com.agrinetwork.service.ProductService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProductsActivity extends AppCompatActivity {
    private RecyclerView allProductRecyclerView;
    private PaginationResponse<Product>  paginationResponseProduct;
    private List<Product> ListSearchProduct = new ArrayList<>();
    private ProductOwnAdapter searchProductAdapter;
    private int page = 1;
    private int limit = 12;
    private final Gson gson = new Gson();
    private boolean hasNext = false;
    private String token;
    private ProductService productService;
    private ProductService.SearchProductCriteria searchProductCriteria;
    private TextInputEditText textSearch;
    private SwipeRefreshLayout refreshLayout;



    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        refreshLayout = findViewById(R.id.swiper_product);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.products_navigation_toggler, R.string.products_navigation_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences sharedPref = getSharedPreferences(Variables.SHARED_TOKENS, Context.MODE_PRIVATE);
        token = sharedPref.getString(Variables.ID_TOKEN_LABEL, "");
        productService = new ProductService(this);
        searchProductCriteria = new ProductService.SearchProductCriteria();
        searchProductCriteria.setPage(page);
        searchProductCriteria.setLimit(limit);

        allProductRecyclerView = findViewById(R.id.product_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        allProductRecyclerView.setLayoutManager(gridLayoutManager);
        searchProductAdapter = new ProductOwnAdapter(ListSearchProduct,this);
        allProductRecyclerView.setAdapter(searchProductAdapter);



        allProductRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleIndex = gridLayoutManager.findLastVisibleItemPosition();
                if(hasNext && lastVisibleIndex >= ListSearchProduct.size() - 2) {
                    loadMoreSearchProduct();
                }
            }
        });


        textSearch = findViewById(R.id.search_text);



        refreshLayout.setOnRefreshListener(()->{
            page = 1;
            searchProductCriteria.setPage(page);
            ListSearchProduct.clear();
            fetchProductSearch();

        });



        fetchProductSearch();

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchProductSearch(){
        Call allSearchProduct = productService.searchProducts(token, searchProductCriteria);
        allSearchProduct.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.setRefreshing(false);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String productResponse = response.body().string();
                    Type productType = new TypeToken<PaginationResponse<Product>>(){}.getType();
                    paginationResponseProduct = gson.fromJson(productResponse,productType);

                    ProductsActivity.this.runOnUiThread(()->{
                        if(!paginationResponseProduct.getDocs().isEmpty()){
                            ListSearchProduct.addAll(paginationResponseProduct.getDocs());
                            searchProductAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                            hasNext = paginationResponseProduct.isHasNextPage();
                        }
                    });
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void loadMoreSearchProduct(){
        page += 1;
        searchProductCriteria.setPage(page);
        Call allSearchProduct = productService.searchProducts(token, searchProductCriteria);
        allSearchProduct.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                refreshLayout.setRefreshing(false);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String productResponse = response.body().string();
                    Type productType = new TypeToken<PaginationResponse<Product>>(){}.getType();
                    paginationResponseProduct = gson.fromJson(productResponse,productType);

                    ProductsActivity.this.runOnUiThread(()->{
                        if(!paginationResponseProduct.getDocs().isEmpty()){
                            ListSearchProduct.addAll(paginationResponseProduct.getDocs());
                            searchProductAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                            hasNext = paginationResponseProduct.isHasNextPage();
                        }
                    });
                }
            }
        });

    }


}