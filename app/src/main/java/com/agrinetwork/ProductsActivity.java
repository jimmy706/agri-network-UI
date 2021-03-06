package com.agrinetwork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.agrinetwork.components.ProductCategoryMenuAdapter;
import com.agrinetwork.components.ProductOwnAdapter;
import com.agrinetwork.config.Variables;
import com.agrinetwork.entities.PaginationResponse;
import com.agrinetwork.entities.product.Product;
import com.agrinetwork.entities.ProductCategory;
import com.agrinetwork.service.CategoryService;
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
    private RecyclerView categoryRecyclerView;
    private PaginationResponse<Product>  paginationResponseProduct;
    private List<Product> ListSearchProduct = new ArrayList<>();
    private final List<ProductCategory> productCategoryList = new ArrayList<>();
    private ProductOwnAdapter searchProductAdapter;
    private ProductCategoryMenuAdapter productCategoryMenuAdapter;
    private int page = 1;
    private int limit = 12;
    private String category;
    private String nameCategory;
    private String nameProduct;
    private final Gson gson = new Gson();
    private boolean hasNext = false;
    private String token;
    private ProductService productService;
    private ProductService.SearchProductCriteria searchProductCriteria;
    private TextInputEditText textSearch;
    private SwipeRefreshLayout refreshLayout;
    private CategoryService categoryService;
    private TextView noResult,showTextResultSearchByCategory,showNameCategory,iconReset;
    private TextView sortName,sortView,sortPrice,sortDate;
    private String numSortName,numSortView,numSortPrice,numSortDate;



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
        textSearch = findViewById(R.id.search_text);
        noResult = findViewById(R.id.no_result_product);
        showTextResultSearchByCategory = findViewById(R.id.show_text_result_search_by_category);
        showNameCategory= findViewById(R.id.show_name_category);
        refreshLayout = findViewById(R.id.swiper_product);
        iconReset = findViewById(R.id.reset_search);

        sortName = findViewById(R.id.sort_name);
        sortView = findViewById(R.id.sort_view);
        sortPrice = findViewById(R.id.sort_price);
        sortDate = findViewById(R.id.sort_date);


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

        categoryService = new CategoryService(this);
        categoryRecyclerView = findViewById(R.id.product_category);
        LinearLayoutManager layoutCategory = new LinearLayoutManager(this);
        categoryRecyclerView.setLayoutManager(layoutCategory);
        productCategoryMenuAdapter = new ProductCategoryMenuAdapter(productCategoryList,this);
        categoryRecyclerView.setAdapter(productCategoryMenuAdapter);



        refreshLayout.setOnRefreshListener(()->{
            page = 1;
            searchProductCriteria.setPage(page);
            ListSearchProduct.clear();
            fetchProductSearch();

        });


        getCategories();


        if(getIntent().hasExtra("idCategory") ){
            category = getIntent().getStringExtra("idCategory");
            nameCategory = getIntent().getStringExtra("nameCategory");
            showNameCategory.setText(nameCategory);
            showTextResultSearchByCategory.setVisibility(View.VISIBLE);
            showNameCategory.setVisibility(View.VISIBLE);

            searchProductCriteria.setCategories(category);

        }
        else if(getIntent().hasExtra("nameProduct")){
            nameProduct = getIntent().getStringExtra("nameProduct");
            textSearch.setText(nameProduct);

            searchProductCriteria.setName(nameProduct);

        }
        else if(getIntent().hasExtra("sortName")){
            numSortName = getIntent().getStringExtra("sortName");
            searchProductCriteria.setSort(Integer.parseInt(numSortName));

            showNameCategory.setText(R.string.sort_product_name);
            showNameCategory.setVisibility(View.VISIBLE);
            showTextResultSearchByCategory.setText(R.string.sort_product_by);
            showTextResultSearchByCategory.setVisibility(View.VISIBLE);

        }
        else if(getIntent().hasExtra("sortView")){
            numSortView = getIntent().getStringExtra("sortView");
            searchProductCriteria.setSort(Integer.parseInt(numSortView));

            showNameCategory.setText(R.string.sort_product_view);
            showNameCategory.setVisibility(View.VISIBLE);
            showTextResultSearchByCategory.setText(R.string.sort_product_by);
            showTextResultSearchByCategory.setVisibility(View.VISIBLE);

        }
        else if(getIntent().hasExtra("sortDate")){
            numSortDate = getIntent().getStringExtra("sortDate");
            searchProductCriteria.setSort(Integer.parseInt(numSortDate));

            showNameCategory.setText(R.string.sort_product_date);
            showNameCategory.setVisibility(View.VISIBLE);
            showTextResultSearchByCategory.setText(R.string.sort_product_by);
            showTextResultSearchByCategory.setVisibility(View.VISIBLE);

        }
        else if(getIntent().hasExtra("sortPrice")){
            numSortPrice = getIntent().getStringExtra("sortPrice");
            searchProductCriteria.setSort(Integer.parseInt(numSortPrice));

            showNameCategory.setText(R.string.sort_product_price);
            showNameCategory.setVisibility(View.VISIBLE);
            showTextResultSearchByCategory.setText(R.string.sort_product_by);
            showTextResultSearchByCategory.setVisibility(View.VISIBLE);

        }



        textSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            String queryName = textView.getText().toString();
            if(i == EditorInfo.IME_ACTION_SEARCH){
               Intent intentSearch = new Intent(this, ProductsActivity.class);
               intentSearch.putExtra("nameProduct",queryName);
               this.startActivity(intentSearch);
                return true;
            }
            return false;
        });

        sortName.setOnClickListener(v->{
            String sort = "1";
            Intent intentSortName = new Intent(this,ProductsActivity.class);
            intentSortName.putExtra("sortName",sort);
            this.startActivity(intentSortName);
        });

        sortView.setOnClickListener(v->{
            String sort = "2";
            Intent intentSortView = new Intent(this,ProductsActivity.class);
            intentSortView.putExtra("sortView",sort);
            this.startActivity(intentSortView);
        });

        sortDate.setOnClickListener(v->{
            String sort = "3";
            Intent intentSortDate = new Intent(this,ProductsActivity.class);
            intentSortDate.putExtra("sortDate",sort);
            this.startActivity(intentSortDate);
        });

        sortPrice.setOnClickListener(v->{
            String sort = "4";
            Intent intentSortPrice = new Intent(this,ProductsActivity.class);
            intentSortPrice.putExtra("sortPrice",sort);
            this.startActivity(intentSortPrice);
        });

        iconReset.setOnClickListener(v->{
            Intent intentReset = new Intent(this,ProductsActivity.class);
            this.startActivity(intentReset);
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
                        else{
                            noResult.setVisibility(View.VISIBLE);
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
                        else{
                            noResult.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

    }

    private void getCategories(){
        Call getProductCategory = categoryService.getCategory(token);
        getProductCategory.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call getProductCategory, @NonNull IOException e) {
                e.printStackTrace();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call getProductCategory, @NonNull Response response) throws IOException {
                if(response.code() == 200){
                    String responseCategory = response.body().string();
                    Type categoryList = new TypeToken<List<ProductCategory>>(){}.getType();
                    List<ProductCategory> productCategory = gson.fromJson(responseCategory,categoryList);

                    ProductsActivity.this.runOnUiThread(()->{
                        productCategoryList.addAll(productCategory);
                        productCategoryMenuAdapter.notifyDataSetChanged();
                    });



                }
            }
        });
    }


}